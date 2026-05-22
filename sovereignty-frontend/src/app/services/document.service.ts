import {inject, Injectable, PLATFORM_ID, signal} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../environments/environment';
import {timer} from 'rxjs';
import {isPlatformBrowser} from '@angular/common';

export type DocStatus = 'UPLOADED' | 'PROCESSING' | 'READY' | 'FAILED';

export interface Document {
  fileName: string,
  id: string,
  status: DocStatus,
  createdAt?: string;
  traceId?: string;
  failureReason?: string;
}

@Injectable({
  providedIn: 'root',
})
export class DocumentService {

  private readonly API_URL = `${environment.apiUrl}/api/documents`;

  private httpClient = inject(HttpClient);

  // platform check
  private platformId = inject(PLATFORM_ID);
  private isBrowser = isPlatformBrowser(this.platformId);

  public documents = signal([] as Document[]);
  private loading = signal(false);
  private eventSources: Map<string, EventSource> = new Map();
  uploadError = signal('');

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

          const msg = err?.error?.message || 'Upload failed. Check logs.';
          this.uploadError.set(msg);

          timer(12_000).subscribe(() => this.uploadError.set(''));

        }
      });
  }

  fetchDocuments() {
    return this.httpClient.get<Document[]>(this.API_URL).subscribe({
      next: response => {
        this.documents.set(response);

        response.forEach(doc => this.subscribeToDocumentStatus(doc.id));
      },
      error: err => console.log('Error fetching docs')
    })
  }

  private subscribeToDocumentStatus(documentId: string) {
    if (!this.isBrowser || typeof EventSource === 'undefined') {
      // optionally log for debugging in dev
      // console.debug('Skipping SSE subscription on server for', documentId);
      return;
    }

    // only subscribe if not already subscribed
    if (this.eventSources.has(documentId)) {
      return;
    }

    const eventSource = new EventSource(`${this.API_URL}/${documentId}/subscribe`);

    eventSource.addEventListener('status-update', (event: any) => {
      let newStatus: DocStatus;
      let traceId: string | undefined;
      let failureReason: string | undefined;

      try {
        const parsed = JSON.parse(event.data);
        newStatus = parsed.status;
        traceId = parsed.traceId;
        failureReason = parsed.failureReason;
      } catch (e) {
        newStatus = event.data as DocStatus;
      }

      console.log(`Document ${documentId} status updated to ${newStatus}`);

      // update the document in the list
      this.documents.update(docs =>
        docs.map(doc => doc.id === documentId ? {
          ...doc,
          status: newStatus,
          traceId: traceId || doc.traceId,
          failureReason: failureReason || doc.failureReason
        } : doc)
      );

      // optionally close connection when ready or failed
      if (newStatus === 'READY' || newStatus === 'FAILED') {
        eventSource.close();
        this.eventSources.delete(documentId);
      }
    });

    eventSource.onerror = (err) => {
      console.error(`SSE connection error for document ${documentId}`, err);
      eventSource.close();
      this.eventSources.delete(documentId);
    };

    this.eventSources.set(documentId, eventSource);
  }

  deleteDocument(document: Document) {
    // close SSE connection if exists
    const es = this.eventSources.get(document.id);
    if (es) {
      es.close();
      this.eventSources.delete(document.id);
    }

    return this.httpClient.delete(this.API_URL, {body: document}).subscribe({
      next: response => this.documents.update(documents => documents.filter(f => f.id !== document.id)),
      error: err => console.log('Error deleting file')
    });
  }

  downloadDocument(document: Document) {
    if (!this.isBrowser) {
      return;
    }

    this.httpClient.get(`${this.API_URL}/${document.id}/download`, {responseType: 'blob'}).subscribe({
      next: blob => {
        const url = URL.createObjectURL(blob);
        const link = window.document.createElement('a');
        link.href = url;
        link.download = document.fileName;
        link.click();
        URL.revokeObjectURL(url);
      },
      error: err => console.log('Error downloading file')
    });
  }
}
