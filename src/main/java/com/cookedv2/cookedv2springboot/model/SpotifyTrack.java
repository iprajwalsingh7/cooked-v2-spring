package com.cookedv2.cookedv2springboot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpotifyTrack {
    private String name;
    private List<SpotifyArtist> artists;
}

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class SpotifyTrackListResponse {
    private List<SpotifyTrack> items;
}
