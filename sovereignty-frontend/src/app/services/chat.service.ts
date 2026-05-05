import {inject, Injectable, signal} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../environments/environment';

export interface ChatMessage {
  message: string;
  isUser: boolean;
}

export interface AnswerResponse {
  response: string
}

@Injectable({
  providedIn: 'root',
})
export class ChatService {

  private readonly API_URL = `${environment.apiUrl}/api/chat/ask`;


  private httpClient = inject(HttpClient);

  messages = signal<ChatMessage[]>([]);

  loading = signal<boolean>(false);

  askQuestion(question: string) {
    this.messages.update(
      lastMessages => [...lastMessages, {message: question, isUser: true}]
    );

    this.loading.set(true);

    this.httpClient.post<AnswerResponse>(this.API_URL, {question})
      .subscribe({
        next: (res) => {
          this.messages.update(
            lastMessages => [...lastMessages, {message: res.response, isUser: false}]
          );
          this.loading.set(false);
        },
        error: err => {
          console.error('Error API:', err);
          this.messages.update(prev => [...prev, {message: "Server unavailable.", isUser: false}]);
          this.loading.set(false);
        }
      })
  }

}
