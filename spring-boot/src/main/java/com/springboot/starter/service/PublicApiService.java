package com.springboot.starter.service;

import com.springboot.starter.model.publicapi.DigitalOceanStatusResponse;
import com.springboot.starter.model.publicapi.DisneyCharactersResponse;
import com.springboot.starter.model.publicapi.DivisionData;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class PublicApiService {

    private final RestTemplate restTemplate;

    public DisneyCharactersResponse getDisneyCharacters() {
        String apiUrl = "https://api.disneyapi.dev/character";
        return restTemplate.getForObject(apiUrl, DisneyCharactersResponse.class);
    }

    public DigitalOceanStatusResponse getDigitalOceanStatus() {
        String apiUrl = "https://status.digitalocean.com/api/v2/status.json";
        return restTemplate.getForObject(apiUrl, DigitalOceanStatusResponse.class);
    }

    public Map<String, DivisionData> getBankHolidays() {
        String apiUrl = "https://www.gov.uk/bank-holidays.json";
        ParameterizedTypeReference<Map<String, DivisionData>> responseType = new ParameterizedTypeReference<>() {
        };
        ResponseEntity<Map<String, DivisionData>> responseEntity =
            restTemplate.exchange(apiUrl, HttpMethod.GET, null, responseType);
        return responseEntity.getBody();
    }
}
