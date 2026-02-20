package com.cookedv2.cookedv2springboot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpotifyArtist {
    private String name;
}

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class SpotifyArtistListResponse {
    private List<SpotifyArtist> items;
}
