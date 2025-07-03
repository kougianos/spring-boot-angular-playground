import { Component, OnInit } from '@angular/core';
import { CacheAnalyticsService } from '../core/services/cache-analytics.service';
import { CacheAnalytics, CacheStats, RateLimitCheckResult } from '../core/models/cache.model';
import { Observable, interval } from 'rxjs';
import { startWith, switchMap, shareReplay } from 'rxjs/operators';

@Component({
  selector: 'app-cache-analytics',
  templateUrl: './cache-analytics.component.html',
  styleUrls: ['./cache-analytics.component.scss']
})
export class CacheAnalyticsComponent implements OnInit {
  analytics$!: Observable<CacheAnalytics>;
  cacheStats$!: Observable<CacheStats[]>;
  isLoading = false;
  error = '';
  infoSectionExpanded = false;

  constructor(private cacheAnalyticsService: CacheAnalyticsService) {}

  ngOnInit() {
    // Auto-refresh every 5 seconds with shared subscription
    this.analytics$ = interval(5000).pipe(
      startWith(0),
      switchMap(() => this.cacheAnalyticsService.getAnalytics()),
      shareReplay(1)
    );

    // Auto-refresh every 10 seconds with shared subscription
    this.cacheStats$ = interval(10000).pipe(
      startWith(0),
      switchMap(() => this.cacheAnalyticsService.getCacheStats()),
      shareReplay(1)
    );
  }

  clearAnalytics() {
    this.isLoading = true;
    this.error = '';
    
    this.cacheAnalyticsService.clearAnalytics().subscribe({
      next: () => {
        this.isLoading = false;
        // Refresh will happen automatically due to interval
      },
      error: (err: any) => {
        this.error = 'Failed to clear analytics: ' + err.message;
        this.isLoading = false;
      }
    });
  }

  evictCacheKey(key: string) {
    this.cacheAnalyticsService.evictCacheKey(key).subscribe({
      next: () => {
        // Refresh will happen automatically due to interval
      },
      error: (err: any) => {
        this.error = 'Failed to evict cache key: ' + err.message;
      }
    });
  }

  checkRateLimit() {
    this.cacheAnalyticsService.checkRateLimit('api-call', 10, 60).subscribe({
      next: (result: RateLimitCheckResult) => {
        console.log('Rate limit check result:', result);
      },
      error: (err: any) => {
        this.error = 'Failed to check rate limit: ' + err.message;
      }
    });
  }

  warmUpCache() {
    this.isLoading = true;
    this.error = '';
    
    this.cacheAnalyticsService.warmUpCache().subscribe({
      next: () => {
        this.isLoading = false;
        // Refresh will happen automatically due to interval
      },
      error: (err: any) => {
        this.error = 'Failed to warm up cache: ' + err.message;
        this.isLoading = false;
      }
    });
  }

  getTopCacheKeysArray(topCacheKeys: { [key: string]: number }): Array<{key: string, count: number}> {
    return Object.entries(topCacheKeys || {})
      .map(([key, count]) => ({ key, count }))
      .sort((a, b) => b.count - a.count)
      .slice(0, 10);
  }

  getOperationCountsArray(operationCounts: { [key: string]: number }): Array<{operation: string, count: number}> {
    return Object.entries(operationCounts || {})
      .map(([operation, count]) => ({ operation, count }))
      .sort((a, b) => b.count - a.count);
  }

  toggleInfoSection() {
    this.infoSectionExpanded = !this.infoSectionExpanded;
  }
}
