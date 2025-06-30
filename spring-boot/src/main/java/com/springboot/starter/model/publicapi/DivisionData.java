package com.springboot.starter.model.publicapi;

import java.util.List;

//https://www.gov.uk/bank-holidays.json
public record DivisionData(
    String division,
    List<BankHolidayEvent> events) {

    public record BankHolidayEvent(
        String title,
        String date,
        String notes,
        boolean bunting) {
    }

}