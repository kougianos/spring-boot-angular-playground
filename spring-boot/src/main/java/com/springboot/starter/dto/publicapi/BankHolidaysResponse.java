package com.springboot.starter.dto.publicapi;

import java.util.List;
import java.util.Map;

//https://www.gov.uk/bank-holidays.json
public record BankHolidaysResponse(
    Map<String, Region> regions
) {

    public record Region(
        String division,
        List<Event> events
    ) {
    }

    public record Event(
        String title,
        String date,
        String notes,
        boolean bunting
    ) {
    }

}