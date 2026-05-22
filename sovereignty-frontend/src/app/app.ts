import {Component, effect, inject, signal} from '@angular/core';
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

  // Error Details Modal state
  selectedTraceId = signal<string | null>(null);
  selectedFailureReason = signal<string | null>(null);
  showErrorModal = signal<boolean>(false);
  copySuccess = signal<boolean>(false);

  constructor() {
    effect(() => {
      this.documentService.fetchDocuments();
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

  onFileSelected(event: any) {
    const file: File = event.target.files[0];
    if (file) {
      this.documentService.uploadDocument(file);
    }
  }
}
