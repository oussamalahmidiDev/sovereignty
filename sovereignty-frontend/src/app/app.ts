import {isPlatformBrowser} from '@angular/common';
import {Component, ElementRef, PLATFORM_ID, ViewChild, effect, inject, signal} from '@angular/core';
import {ChatService} from './services/chat.service';
import {DocumentService} from './services/document.service';
import {HealthService} from './services/health.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {

  public chatService = inject(ChatService);
  public documentService = inject(DocumentService);
  public healthService = inject(HealthService);
  private readonly isBrowser = isPlatformBrowser(inject(PLATFORM_ID));

  @ViewChild('chatScrollContainer') private chatScrollContainer?: ElementRef<HTMLElement>;
  @ViewChild('userInput') private userInput?: ElementRef<HTMLInputElement>;

  // Error Details Modal state
  selectedTraceId = signal<string | null>(null);
  selectedFailureReason = signal<string | null>(null);
  showErrorModal = signal<boolean>(false);
  copySuccess = signal<boolean>(false);
  private scrollAnimationFrame?: number;

  constructor() {
    effect(() => {
      this.chatService.fetchChats();
    });

    effect(() => {
      this.documentService.fetchDocuments();
    });

    effect(() => {
      this.chatService.messages().map(message => message.message).join('');
      this.chatService.showLoadingBubble();
      this.scheduleChatScrollToBottom();
    });
  }

  private scheduleChatScrollToBottom() {
    if (!this.isBrowser) {
      return;
    }

    if (this.scrollAnimationFrame) {
      cancelAnimationFrame(this.scrollAnimationFrame);
    }

    this.scrollAnimationFrame = requestAnimationFrame(() => {
      const container = this.chatScrollContainer?.nativeElement;
      if (!container) {
        return;
      }

      if (typeof container.scrollTo === 'function') {
        container.scrollTo({
          top: container.scrollHeight,
          behavior: 'auto',
        });
      } else {
        container.scrollTop = container.scrollHeight;
      }
    });
  }

  openErrorModal(traceId?: string, failureReason?: string) {
    this.selectedTraceId.set(traceId || 'N/A');
    this.selectedFailureReason.set(failureReason || 'No failure reason provided.');
    this.showErrorModal.set(true);
    this.copySuccess.set(false);
  }

  closeErrorModal() {
    this.showErrorModal.set(false);
    this.selectedTraceId.set(null);
    this.selectedFailureReason.set(null);
    this.copySuccess.set(false);
  }

  copyTraceId() {
    const traceId = this.selectedTraceId();
    if (traceId && traceId !== 'N/A') {
      navigator.clipboard.writeText(traceId).then(() => {
        this.copySuccess.set(true);
        setTimeout(() => this.copySuccess.set(false), 2000);
      });
    }
  }

  onSend(inputElement: HTMLInputElement) {
    const question = inputElement.value.trim();

    if (question.length > 0) {
      this.chatService.askQuestion(question);

      inputElement.value = '';
    }
  }

  startNewChat() {
    this.chatService.startNewChat();
    requestAnimationFrame(() => {
      const inputElement = this.userInput?.nativeElement;
      if (inputElement) {
        inputElement.value = '';
        inputElement.focus();
      }
    });
  }

  onFileSelected(event: any) {
    const file: File = event.target.files[0];
    if (file) {
      this.documentService.uploadDocument(file);
    }
  }
}
