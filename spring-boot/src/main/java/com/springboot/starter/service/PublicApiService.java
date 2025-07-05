package com.springboot.starter.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.springboot.starter.model.publicapi.DigitalOceanStatusResponse;
import com.springboot.starter.model.publicapi.DisneyCharactersResponse;
import com.springboot.starter.model.publicapi.DivisionData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PublicApiService {

    private final RestTemplate restTemplate;
    private final CacheService cacheService;

    public DisneyCharactersResponse getDisneyCharacters() {
        return getDisneyCharacters("system");
    }

    public DisneyCharactersResponse getDisneyCharacters(String userId) {
        return cacheService.getOrSet(
            "public-api:disney-characters",
            () -> {
                String apiUrl = "https://api.disneyapi.dev/character";
                return restTemplate.getForObject(apiUrl, DisneyCharactersResponse.class);
            },
            Duration.ofMinutes(30),
            userId,
            DisneyCharactersResponse.class
        );
    }

    public DigitalOceanStatusResponse getDigitalOceanStatus() {
        return getDigitalOceanStatus("system");
    }

    public DigitalOceanStatusResponse getDigitalOceanStatus(String userId) {
        return cacheService.getOrSet(
            "public-api:digital-ocean-status",
            () -> {
                String apiUrl = "https://status.digitalocean.com/api/v2/status.json";
                return restTemplate.getForObject(apiUrl, DigitalOceanStatusResponse.class);
            },
            Duration.ofMinutes(15),
            userId,
            DigitalOceanStatusResponse.class
        );
    }

    public Map<String, DivisionData> getBankHolidays() {
        return getBankHolidays("system");
    }

    public Map<String, DivisionData> getBankHolidays(String userId) {
        return cacheService.getOrSet(
            "public-api:bank-holidays",
            () -> {
                String apiUrl = "https://www.gov.uk/bank-holidays.json";
                ParameterizedTypeReference<Map<String, DivisionData>> responseType =
                    new ParameterizedTypeReference<>() {
                    };
                ResponseEntity<Map<String, DivisionData>> responseEntity =
                    restTemplate.exchange(apiUrl, HttpMethod.GET, null, responseType);
                return responseEntity.getBody();
            },
            Duration.ofHours(2),
            userId,
            new TypeReference<>() {
            }
        );
    }
}
