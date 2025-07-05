package com.springboot.starter.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class CacheWarmingService {

    private final TestDocumentService testDocumentService;
    private final PublicApiService publicApiService;

    @EventListener(ApplicationReadyEvent.class)
    public void warmUpCache() {
        log.info("Starting cache warming process...");

        try {
            // Warm up test documents cache
            warmUpTestDocuments();

            // Warm up public API responses (with longer TTL)
            warmUpPublicApis();

            log.info("Cache warming completed successfully");
        } catch (Exception e) {
            log.error("Cache warming failed", e);
        }
    }

    private void warmUpTestDocuments() {
        try {
            log.debug("Warming up test documents cache...");
            testDocumentService.getAllTestDocuments("cache-warming");
            log.debug("Test documents cache warmed up");
        } catch (Exception e) {
            log.warn("Failed to warm up test documents cache", e);
        }
    }

    private void warmUpPublicApis() {
        try {
            log.debug("Warming up public APIs cache...");

            publicApiService.getDisneyCharacters("cache-warming");
            publicApiService.getDigitalOceanStatus("cache-warming");
            publicApiService.getBankHolidays("cache-warming");

            log.debug("Public APIs cache warmed up");
        } catch (Exception e) {
            log.warn("Failed to warm up public APIs cache", e);
        }
    }

    public void manualCacheWarmup() {
        log.info("Manual cache warming initiated");
        warmUpCache();
    }
}
