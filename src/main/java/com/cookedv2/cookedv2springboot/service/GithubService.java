package com.cookedv2.cookedv2springboot.service;

import com.cookedv2.cookedv2springboot.model.GithubProfile;
import com.cookedv2.cookedv2springboot.model.GithubRepo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

@Service
public class GithubService {

    @Value("${github.token:}")
    private String githubToken;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private static final String GITHUB_API_URL = "https://api.github.com";

    public GithubService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    // Helper method to add Auth header if token exists
    private HttpRequest.Builder authBuilder(HttpRequest.Builder builder) {
        if (githubToken != null && !githubToken.isEmpty()) {
            builder.header("Authorization", "Bearer " + githubToken);
        }
        return builder.header("Accept", "application/vnd.github.v3+json");
    }

    public GithubProfile getProfile(String username) {
        try {
            HttpRequest request = authBuilder(HttpRequest.newBuilder()
                    .uri(URI.create(GITHUB_API_URL + "/users/" + username))
                    .GET())
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 404) {
                return null;
            } else if (response.statusCode() != 200) {
                throw new RuntimeException("GitHub API error fetching profile: " + response.statusCode());
            }

            return objectMapper.readValue(response.body(), GithubProfile.class);

        } catch (Exception e) {
            System.err.println("GitHub service error: " + e.getMessage());
            return null;
        }
    }

    public List<GithubRepo> getRepos(String username) {
        try {
            // Fetching up to 100 repositories, sorted by updated
            HttpRequest request = authBuilder(HttpRequest.newBuilder()
                    .uri(URI.create(GITHUB_API_URL + "/users/" + username + "/repos?per_page=100&sort=updated"))
                    .GET())
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("GitHub API error fetching repos: " + response.statusCode());
            }

            return objectMapper.readValue(response.body(), new TypeReference<List<GithubRepo>>() {
            });

        } catch (Exception e) {
            System.err.println("GitHub service error: " + e.getMessage());
            return List.of();
        }
    }
}
