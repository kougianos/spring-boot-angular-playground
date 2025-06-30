package com.springboot.starter.model.publicapi;

//https://status.digitalocean.com/api/v2/status.json
public record DigitalOceanStatusResponse(
    Page page,
    Status status
) {
    public record Page(
        String id,
        String name,
        String url,
        String time_zone,
        String updated_at
    ) {
    }

    public record Status(
        String indicator,
        String description
    ) {
    }
}
