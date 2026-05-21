import {Component, effect, inject} from '@angular/core';
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

  constructor() {
    effect(() => {
      this.documentService.fetchDocuments();
    });
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
