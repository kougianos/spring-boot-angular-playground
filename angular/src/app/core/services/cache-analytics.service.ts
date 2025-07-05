import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CacheAnalytics, CacheStats, RateLimitCheckResult } from '../models/cache.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class CacheAnalyticsService {
  private readonly apiUrl = `${environment.apiUrl}/cache`;

  constructor(private readonly http: HttpClient) {}

  getAnalytics(): Observable<CacheAnalytics> {
    return this.http.get<CacheAnalytics>(`${this.apiUrl}/analytics`);
  }

  getCacheStats(): Observable<CacheStats[]> {
    return this.http.get<CacheStats[]>(`${this.apiUrl}/stats`);
  }

  clearAnalytics(): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.apiUrl}/clear`, {});
  }

  evictCacheKey(key: string): Observable<{ message: string; key: string }> {
    return this.http.post<{ message: string; key: string }>(`${this.apiUrl}/evict/${encodeURIComponent(key)}`, {});
  }

  checkRateLimit(
    operation: string = 'api-call',
    maxRequests: number = 10,
    windowSeconds: number = 60
  ): Observable<RateLimitCheckResult> {
    return this.http.get<RateLimitCheckResult>(
      `${this.apiUrl}/rate-limit-check?operation=${operation}&maxRequests=${maxRequests}&windowSeconds=${windowSeconds}`
    );
  }

  warmUpCache(): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.apiUrl}/warm-up`, {});
  }
}
