export interface CacheAnalytics {
  totalRequests: number;
  cacheHits: number;
  cacheMisses: number;
  hitRate: number;
  avgResponseTimeMs: number;
  topCacheKeys: { [key: string]: number };
  operationCounts: { [key: string]: number };
  lastUpdated: string;
  rateLimitStats: RateLimitStats;
}

export interface RateLimitStats {
  totalRateLimitChecks: number;
  rateLimitExceeded: number;
  rateLimitByUser: { [key: string]: number };
}

export interface CacheStats {
  cacheKey: string;
  hitCount: number;
  missCount: number;
  hitRate: number;
  avgResponseTimeMs: number;
  lastAccessed: string;
  createdAt: string;
  dataType: string;
}

export interface RateLimitCheckResult {
  allowed: boolean;
  operation: string;
  maxRequests: number;
  windowSeconds: number;
}
