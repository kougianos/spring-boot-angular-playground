package com.springboot.starter.controller;

import com.springboot.starter.model.cache.CacheAnalytics;
import com.springboot.starter.model.cache.CacheStats;
import com.springboot.starter.service.CacheAnalyticsService;
import com.springboot.starter.service.CacheService;
import com.springboot.starter.service.CacheWarmingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cache")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class CacheController {

    private final CacheAnalyticsService cacheAnalyticsService;
    private final CacheService cacheService;
    private final CacheWarmingService cacheWarmingService;

    @GetMapping("/analytics")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<CacheAnalytics> getCacheAnalytics() {
        CacheAnalytics analytics = cacheAnalyticsService.getGlobalAnalytics();
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<CacheStats>> getCacheStats() {
        List<CacheStats> stats = cacheAnalyticsService.getAllCacheStats();
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/clear")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> clearCache() {
        cacheAnalyticsService.clearAnalytics();
        return ResponseEntity.ok(Map.of("message", "Cache analytics cleared successfully"));
    }

    @PostMapping("/evict/{key}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> evictCacheKey(@PathVariable String key) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        
        cacheService.evict(key, userDetails.getUsername());
        return ResponseEntity.ok(Map.of("message", "Cache key evicted successfully", "key", key));
    }

    @GetMapping("/rate-limit-check")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> checkRateLimit(
            @RequestParam(defaultValue = "api-call") String operation,
            @RequestParam(defaultValue = "10") int maxRequests,
            @RequestParam(defaultValue = "60") int windowSeconds) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        
        boolean allowed = cacheService.checkRateLimit(
            userDetails.getUsername(),
            operation,
            maxRequests,
            Duration.ofSeconds(windowSeconds)
        );
        
        return ResponseEntity.ok(Map.of(
            "allowed", allowed,
            "operation", operation,
            "maxRequests", maxRequests,
            "windowSeconds", windowSeconds
        ));
    }

    @PostMapping("/warm-up")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> warmUpCache() {
        cacheWarmingService.manualCacheWarmup();
        return ResponseEntity.ok(Map.of("message", "Cache warming initiated successfully"));
    }
}
