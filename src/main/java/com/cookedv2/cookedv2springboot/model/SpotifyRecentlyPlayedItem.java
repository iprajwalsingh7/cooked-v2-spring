package com.cookedv2.cookedv2springboot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpotifyRecentlyPlayedItem {
    private SpotifyTrack track;
}

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class SpotifyRecentlyPlayedResponse {
    private List<SpotifyRecentlyPlayedItem> items;
}
