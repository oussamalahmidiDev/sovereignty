import {Component, inject, signal} from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {ChatService} from './services/chat.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {

  public chatService = inject(ChatService);

  onSend(inputElement: HTMLInputElement) {
    const question = inputElement.value.trim();

    if (question.length > 0) {
      this.chatService.askQuestion(question);

      inputElement.value = '';
    }
  }
}
