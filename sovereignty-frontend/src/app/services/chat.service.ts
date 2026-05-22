import {computed, inject, Injectable, signal} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../environments/environment';

export interface ChatMessage {
  message: string;
  isUser: boolean;
}

export interface AnswerResponse {
  response: string;
  chatId: string;
}

export interface ChatSummary {
  id: string;
  name: string;
  createdAt: string;
  updatedAt: string;
}

export interface ChatMessageResponse {
  id: string;
  chatId: string;
  role: 'USER' | 'ASSISTANT';
  content: string;
  createdAt: string;
}

@Injectable({
  providedIn: 'root',
})
export class ChatService {

  private readonly API_URL = `${environment.apiUrl}/api/chat`;
  private readonly STREAM_API_URL = `${this.API_URL}/ask/stream`;
  private readonly httpClient = inject(HttpClient);

  messages = signal<ChatMessage[]>([]);
  chats = signal<ChatSummary[]>([]);
  selectedChatId = signal<string | null>(null);
  loading = signal<boolean>(false);

  showLoadingBubble = computed(() => {
    if (!this.loading()) {
      return false;
    }
    const msgs = this.messages();
    if (msgs.length === 0) {
      return true;
    }
    const lastMsg = msgs[msgs.length - 1];
    return lastMsg.isUser || !lastMsg.message;
  });

  private abortController: AbortController | null = null;

  fetchChats() {
    return this.httpClient.get<ChatSummary[]>(this.API_URL).subscribe({
      next: response => this.chats.set(response),
      error: err => console.log('Error fetching chats')
    });
  }

  selectChat(chat: ChatSummary) {
    this.selectedChatId.set(chat.id);

    return this.httpClient.get<ChatMessageResponse[]>(`${this.API_URL}/${chat.id}/messages`).subscribe({
      next: response => {
        this.messages.set(response.map(message => ({
          message: message.content,
          isUser: message.role === 'USER'
        })));
      },
      error: err => console.log('Error fetching chat messages')
    });
  }

  startNewChat() {
    this.selectedChatId.set(null);
    this.messages.set([]);
  }

  deleteChat(chat: ChatSummary) {
    return this.httpClient.delete(`${this.API_URL}/${chat.id}`).subscribe({
      next: () => {
        this.chats.update(chats => chats.filter(existingChat => existingChat.id !== chat.id));
        if (this.selectedChatId() === chat.id) {
          this.startNewChat();
        }
      },
      error: err => console.log('Error deleting chat')
    });
  }

  stopGeneration() {
    if (this.abortController) {
      this.abortController.abort();
      this.abortController = null;
    }
    this.loading.set(false);
  }

  async askQuestion(question: string) {
    this.messages.update(
      lastMessages => [...lastMessages, {message: question, isUser: true}]
    );

    this.loading.set(true);
    this.abortController = new AbortController();

    // Add empty assistant message that will be updated chunk by chunk
    this.messages.update(
      lastMessages => [...lastMessages, {message: '', isUser: false}]
    );

    try {
      const response = await fetch(this.STREAM_API_URL, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'text/event-stream'
        },
        body: JSON.stringify({ question, chatId: this.selectedChatId() }),
        signal: this.abortController.signal
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const reader = response.body?.getReader();
      const decoder = new TextDecoder();
      if (!reader) {
        throw new Error('ReadableStream not supported in response');
      }

      let done = false;
      let assistantText = '';
      let buffer = '';

      while (!done) {
        const { value, done: doneReading } = await reader.read();
        done = doneReading;
        if (value) {
          buffer += decoder.decode(value, { stream: !done });
          
          // SSE events are separated by double newlines \n\n
          const events = buffer.split('\n\n');
          // The last element might be an incomplete chunk, keep it in the buffer
          buffer = events.pop() || '';

          for (const event of events) {
            const lines = event.split('\n');
            for (const line of lines) {
              if (line.startsWith('data:')) {
                const dataStr = line.slice(5).trim();
                if (dataStr) {
                  try {
                    const dataObj = JSON.parse(dataStr);
                    if (dataObj && dataObj.chatId) {
                      this.selectedChatId.set(dataObj.chatId);
                    }
                    if (dataObj && dataObj.response) {
                      assistantText += dataObj.response;
                      this.messages.update(lastMessages => {
                        const updated = [...lastMessages];
                        if (updated.length > 0) {
                          updated[updated.length - 1] = { message: assistantText, isUser: false };
                        }
                        return updated;
                      });
                    }
                  } catch (e) {
                    console.warn('Error parsing SSE event data:', e, dataStr);
                  }
                }
              }
            }
          }
        }
      }

      // If anything remains in the buffer after stream closes
      if (buffer) {
        const lines = buffer.split('\n');
        for (const line of lines) {
          if (line.startsWith('data:')) {
            const dataStr = line.slice(5).trim();
            if (dataStr) {
              try {
                const dataObj = JSON.parse(dataStr);
                if (dataObj && dataObj.chatId) {
                  this.selectedChatId.set(dataObj.chatId);
                }
                if (dataObj && dataObj.response) {
                  assistantText += dataObj.response;
                  this.messages.update(lastMessages => {
                    const updated = [...lastMessages];
                    if (updated.length > 0) {
                      updated[updated.length - 1] = { message: assistantText, isUser: false };
                    }
                    return updated;
                  });
                }
              } catch (e) {
                console.warn('Error parsing final SSE event data:', e, dataStr);
              }
            }
          }
        }
      }

    } catch (err: any) {
      if (err.name === 'AbortError') {
        console.log('Streaming aborted by user.');
        this.messages.update(prev => {
          const lastMsg = prev[prev.length - 1];
          if (lastMsg && !lastMsg.isUser && !lastMsg.message) {
            // Remove the empty assistant message
            return prev.slice(0, -1);
          }
          return prev;
        });
      } else {
        console.error('Error API:', err);
        this.messages.update(prev => {
          const updated = [...prev];
          if (updated.length > 0) {
            updated[updated.length - 1] = { message: "Server unavailable.", isUser: false };
          }
          return updated;
        });
      }
    } finally {
      this.abortController = null;
      this.loading.set(false);
      this.fetchChats();
    }
  }

}
