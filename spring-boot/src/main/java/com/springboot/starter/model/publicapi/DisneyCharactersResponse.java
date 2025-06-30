package com.springboot.starter.model.publicapi;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

//https://api.disneyapi.dev/character
public record DisneyCharactersResponse(
    Info info,
    List<Character> data
) {
    public record Info(
        int count,
        int totalPages,
        String previousPage,
        String nextPage
    ) {
    }

    public record Character(
        @JsonProperty("_id")
        int id,

        List<String> films,
        List<String> shortFilms,
        List<String> tvShows,
        List<String> videoGames,
        List<String> parkAttractions,
        List<String> allies,
        List<String> enemies,

        String sourceUrl,
        String name,
        String imageUrl,
        String createdAt,
        String updatedAt,
        String url,

        @JsonProperty("__v")
        int version
    ) {
    }
}
