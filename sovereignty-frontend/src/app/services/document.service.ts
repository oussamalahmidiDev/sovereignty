import {inject, Injectable, signal} from '@angular/core';
import {HttpClient} from '@angular/common/http';

@Injectable({
  providedIn: 'root',
})
export class DocumentService {

  private readonly API_URL = 'http://localhost:8080/api/documents/upload';

  private httpClient = inject(HttpClient);

  private loading = signal(false);

  uploadDocument(file: File) {
    const formData = new FormData();
    formData.append('file', file);

    this.loading.set(true);

    return this.httpClient.post(this.API_URL, formData)
      .subscribe({
        next: () => {
          this.loading.set(false);
          alert("Document indexé !");
        },
        error: (err) => {
          console.error('Upload failed', err);
          this.loading.set(false);
        }
      });
  }
}
