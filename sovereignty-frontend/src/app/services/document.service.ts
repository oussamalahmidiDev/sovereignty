import {inject, Injectable, signal} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../environments/environment';

export interface Document {
  fileName: string,
  id: string
}

@Injectable({
  providedIn: 'root',
})
export class DocumentService {

  private readonly API_URL = `${environment.apiUrl}/api/documents`;

  private httpClient = inject(HttpClient);

  public documents = signal([] as Document[]);
  private loading = signal(false);

  uploadDocument(file: File) {
    const formData = new FormData();
    formData.append('file', file);

    this.loading.set(true);

    return this.httpClient.post(`${this.API_URL}/upload`, formData)
      .subscribe({
        next: () => {
          this.loading.set(false);
          this.fetchDocuments();
        },
        error: (err) => {
          console.error('Upload failed', err);
          this.loading.set(false);
        }
      });
  }

  fetchDocuments() {
    return this.httpClient.get<Document[]>(this.API_URL).subscribe({
      next: response => this.documents.set(response),
      error: err => console.log('Error fetching docs')
    })
  }

  deleteDocument(document: Document) {
    return this.httpClient.delete(this.API_URL, {body: document}).subscribe({
      next: response => this.documents.update(documents => documents.filter(f => f.id !== document.id)),
      error: err => console.log('Error deleting file')
    });
  }
}
