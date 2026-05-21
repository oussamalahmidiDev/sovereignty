import {inject, Injectable, signal} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../environments/environment';
import {interval} from 'rxjs';

export interface HealthStatus {
  status: 'UP' | 'DOWN';
  message: string;
  details: {
    ollama: boolean;
    database: boolean;
    kafka: boolean;
  };
}

@Injectable({
  providedIn: 'root',
})
export class HealthService {
  private readonly HEALTH_URL = `${environment.apiUrl}/api/health/status`;
  private httpClient = inject(HttpClient);

  healthStatus = signal<HealthStatus | null>(null);
  isHealthy = signal<boolean>(false);

  constructor() {
    // Check health on startup
    this.checkHealth();
    // Poll every 30 seconds
    interval(30000).subscribe(() => this.checkHealth());
  }

  checkHealth() {
    this.httpClient.get<HealthStatus>(this.HEALTH_URL).subscribe({
      next: (res) => {
        this.healthStatus.set(res);
        this.isHealthy.set(res.status === 'UP');
      },
      error: (err) => {
        console.error('Health check failed:', err);
        this.healthStatus.set({
          status: 'DOWN',
          message: 'Unable to connect to backend',
          details: { ollama: false, database: false, kafka: false }
        });
        this.isHealthy.set(false);
      }
    });
  }
}
