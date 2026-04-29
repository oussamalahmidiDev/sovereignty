import {Component, inject, signal} from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {ChatService} from './services/chat.service';
import {DocumentService} from './services/document.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {

  public chatService = inject(ChatService);
  public documentService = inject(DocumentService);

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
