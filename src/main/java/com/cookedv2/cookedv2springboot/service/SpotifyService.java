package com.cookedv2.cookedv2springboot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class SpotifyService {

    @Value("${spotify.client.id}")
    private String clientId;

    @Value("${spotify.client.secret}")
    private String clientSecret;

    @Value("${spotify.redirect.uri}")
    private String redirectUri;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private static final String SPOTIFY_API_URL = "https://api.spotify.com/v1/me";

    public SpotifyService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public String exchangeToken(String code) {
        try {
            String encodedString = java.util.Base64.getEncoder()
                    .encodeToString((clientId + ":" + clientSecret).getBytes());

            String requestBody = "grant_type=authorization_code"
                    + "&code=" + code
                    + "&redirect_uri=" + redirectUri;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://accounts.spotify.com/api/token"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Authorization", "Basic " + encodedString)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed to exchange Spotify token: " + response.body());
            }

            JsonNode rootNode = objectMapper.readTree(response.body());
            return rootNode.path("access_token").asText();

        } catch (Exception e) {
            throw new RuntimeException("Spotify Token Exchange Error: " + e.getMessage());
        }
    }

    private JsonNode fetchSpotifyData(String endpoint, String accessToken) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SPOTIFY_API_URL + endpoint))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Spotify API fetch error: " + response.statusCode());
        }

        return objectMapper.readTree(response.body());
    }

    public String getProfileName(String accessToken) {
        try {
            JsonNode node = fetchSpotifyData("", accessToken);
            return node.path("display_name").asText();
        } catch (Exception e) {
            return "Unknown User";
        }
    }

    public String getTopArtists(String accessToken) {
        try {
            JsonNode node = fetchSpotifyData("/top/artists?limit=10&time_range=medium_term", accessToken);
            JsonNode items = node.path("items");
            StringBuilder artists = new StringBuilder();
            if (items.isArray()) {
                for (int i = 0; i < items.size(); i++) {
                    artists.append(items.get(i).path("name").asText());
                    if (i < items.size() - 1)
                        artists.append(", ");
                }
            }
            return artists.toString();
        } catch (Exception e) {
            return "None";
        }
    }

    public String getTopTracks(String accessToken) {
        try {
            JsonNode node = fetchSpotifyData("/top/tracks?limit=10&time_range=medium_term", accessToken);
            JsonNode items = node.path("items");
            StringBuilder tracks = new StringBuilder();
            if (items.isArray()) {
                for (int i = 0; i < items.size(); i++) {
                    tracks.append(items.get(i).path("name").asText()).append(" by ")
                            .append(items.get(i).path("artists").get(0).path("name").asText());
                    if (i < items.size() - 1)
                        tracks.append(", ");
                }
            }
            return tracks.toString();
        } catch (Exception e) {
            return "None";
        }
    }

    public String getRecentlyPlayed(String accessToken) {
        try {
            JsonNode node = fetchSpotifyData("/player/recently-played?limit=10", accessToken);
            JsonNode items = node.path("items");
            StringBuilder recent = new StringBuilder();
            if (items.isArray()) {
                for (int i = 0; i < items.size(); i++) {
                    JsonNode trackNode = items.get(i).path("track");
                    recent.append(trackNode.path("name").asText()).append(" by ")
                            .append(trackNode.path("artists").get(0).path("name").asText());
                    if (i < items.size() - 1)
                        recent.append(", ");
                }
            }
            return recent.toString();
        } catch (Exception e) {
            return "None";
        }
    }
}
