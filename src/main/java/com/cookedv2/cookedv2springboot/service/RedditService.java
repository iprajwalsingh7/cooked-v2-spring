package com.cookedv2.cookedv2springboot.service;

import com.cookedv2.cookedv2springboot.model.RedditActivity;
import com.cookedv2.cookedv2springboot.model.RedditProfile;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class RedditService {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private static final String REDDIT_API_URL = "https://www.reddit.com/user/";

    public RedditService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public RedditProfile getProfile(String username) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(REDDIT_API_URL + username + "/about.json"))
                    .header("User-Agent", "CookedRoastApp/1.0")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 404) {
                return null;
            } else if (response.statusCode() != 200) {
                throw new RuntimeException("Reddit API error fetching profile: " + response.statusCode());
            }

            JsonNode rootNode = objectMapper.readTree(response.body());
            JsonNode dataNode = rootNode.path("data");

            if (dataNode.isMissingNode() || dataNode.isNull() || dataNode.isEmpty()) {
                return null; // Handle shadowbanned or empty profiles safely
            }
            return objectMapper.treeToValue(dataNode, RedditProfile.class);

        } catch (Exception e) {
            System.err.println("Reddit service error: " + e.getMessage());
            return null;
        }
    }

    public List<RedditActivity> getHistory(String username) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(REDDIT_API_URL + username + ".json?limit=50"))
                    .header("User-Agent", "CookedRoastApp/1.0")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Reddit API error fetching history: " + response.statusCode());
            }

            JsonNode rootNode = objectMapper.readTree(response.body());
            JsonNode childrenNode = rootNode.path("data").path("children");

            List<RedditActivity> activities = new ArrayList<>();
            if (childrenNode.isArray()) {
                for (JsonNode child : childrenNode) {
                    JsonNode dataNode = child.path("data");
                    activities.add(objectMapper.treeToValue(dataNode, RedditActivity.class));
                }
            }
            return activities;

        } catch (Exception e) {
            System.err.println("Reddit history service error: " + e.getMessage());
            return List.of();
        }
    }
}
