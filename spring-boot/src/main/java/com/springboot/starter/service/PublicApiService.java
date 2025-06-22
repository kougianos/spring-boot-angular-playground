package com.springboot.starter.service;

import com.springboot.starter.dto.publicapi.BankHolidaysResponse;
import com.springboot.starter.dto.publicapi.DigitalOceanStatusResponse;
import com.springboot.starter.dto.publicapi.DisneyCharactersResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
    
    public BankHolidaysResponse getBankHolidays() {
        String apiUrl = "https://www.gov.uk/bank-holidays.json";
        return restTemplate.getForObject(apiUrl, BankHolidaysResponse.class);
    }
}
