package com.cookedv2.cookedv2springboot.controller;

import com.cookedv2.cookedv2springboot.model.GithubProfile;
import com.cookedv2.cookedv2springboot.model.GithubRepo;
import com.cookedv2.cookedv2springboot.model.RoastRequest;
import com.cookedv2.cookedv2springboot.model.RoastResponse;
import com.cookedv2.cookedv2springboot.model.TokenRequest;
import com.cookedv2.cookedv2springboot.service.GeminiService;
import com.cookedv2.cookedv2springboot.service.GithubService;
import com.cookedv2.cookedv2springboot.service.RedditService;
import com.cookedv2.cookedv2springboot.service.SpotifyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/roast")
@CrossOrigin(origins = "*")
public class RoastController {

    @Autowired
    private GithubService githubService;

    @Autowired
    private RedditService redditService;

    @Autowired
    private SpotifyService spotifyService;

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("/github")
    public ResponseEntity<RoastResponse> roastGithub(@RequestBody RoastRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(RoastResponse.error("Username is required"));
        }

        try {
            GithubProfile profile = githubService.getProfile(request.getUsername());

            if (profile == null) {
                return ResponseEntity.status(404).body(RoastResponse.error("GitHub user not found"));
            }

            List<GithubRepo> repos = githubService.getRepos(request.getUsername());

            // Get Top 20 repositories to prevent prompt injection limit overflowing
            List<GithubRepo> topRepos = repos.stream().limit(20).collect(Collectors.toList());

            // Build Prompt
            String repoJson = objectMapper.writeValueAsString(topRepos);
            String prompt = String.format(
                    "Roast this GitHub user based on their profile and repositories. Be a toxic senior engineer reviewing a junior's terrible code.\n\n"
                            +
                            "Profile:\n" +
                            "- Username: %s\n" +
                            "- Bio: %s\n" +
                            "- Followers: %d\n" +
                            "- Public Repos: %d\n" +
                            "- Created At: %s\n\n" +
                            "Repositories (Top 20):\n" +
                            "%s\n\n" +
                            "Instructions:\n" +
                            "1. Be BRUTAL. If they have many forks, call them a \"copy-paste developer.\"\n" +
                            "2. If they have many empty repos, ask if they have commitment issues.\n" +
                            "3. If they use HTML/CSS, tell them they aren't a real programmer.\n" +
                            "4. If they use Python/JS, mock their lack of type safety or performance.\n" +
                            "5. Use terms like \"spaghetti code,\" \"tech debt,\" \"tutorial hell,\" \"10x engineer (derogatory).\"\n"
                            +
                            "6. Keep it under 200 words.",
                    profile.getLogin(),
                    profile.getBio() != null ? profile.getBio() : "None",
                    profile.getFollowers(),
                    profile.getPublic_repos(),
                    profile.getCreated_at(),
                    repoJson);

            // Fetch from Gemini
            String roastText = geminiService.generateRoast(prompt);
            return ResponseEntity.ok(RoastResponse.success(roastText));

        } catch (Exception e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Failed to generate roast";
            if (errorMessage.contains("rate limit") || errorMessage.contains("overheating")) {
                return ResponseEntity.status(429).body(RoastResponse.error(errorMessage));
            }
            return ResponseEntity.internalServerError().body(RoastResponse.error(errorMessage));
        }
    }

    @PostMapping("/reddit")
    public ResponseEntity<RoastResponse> roastReddit(@RequestBody RoastRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(RoastResponse.error("Username is required"));
        }

        try {
            com.cookedv2.cookedv2springboot.model.RedditProfile profile = redditService
                    .getProfile(request.getUsername());

            if (profile == null) {
                return ResponseEntity.status(404).body(RoastResponse.error("Reddit user not found or shadowbanned"));
            }

            List<com.cookedv2.cookedv2springboot.model.RedditActivity> history = redditService
                    .getHistory(request.getUsername());

            List<com.cookedv2.cookedv2springboot.model.RedditActivity> comments = history.stream()
                    .filter(a -> a.getBody() != null)
                    .limit(20)
                    .collect(Collectors.toList());

            List<com.cookedv2.cookedv2springboot.model.RedditActivity> posts = history.stream()
                    .filter(a -> a.getTitle() != null)
                    .limit(10)
                    .collect(Collectors.toList());

            // Build Prompt
            String activitiesJson = objectMapper
                    .writeValueAsString(java.util.Map.of("comments", comments, "posts", posts));

            // Format created_utc timestamp properly (convert seconds to millis)
            java.util.Date createdAt = new java.util.Date((long) (profile.getCreated_utc() * 1000));

            String prompt = String.format(
                    "Roast this Reddit user based on their profile and history. You are a cynical internet troll who hates everyone.\n\n"
                            +
                            "Profile:\n" +
                            "- Username: %s\n" +
                            "- Karma: %d\n" +
                            "- Account Created: %s\n\n" +
                            "Recent Activity:\n" +
                            "%s\n\n" +
                            "Instructions:\n" +
                            "1. Destroy them based on their subreddits. If they post in gaming subs, call them a \"sweaty gamer.\"\n"
                            +
                            "2. If they have high karma, tell them to \"touch grass\" and \"get a job.\"\n" +
                            "3. If they have low karma, call them a \"lurker\" or \"irrelevant.\"\n" +
                            "4. Mock their specific comments. Quote them if they said something cringe.\n" +
                            "5. Use terms like \"chronically online,\" \"neckbeard,\" \"soyjak,\" \"NPC behavior.\"\n"
                            +
                            "6. Keep it under 200 words.",
                    profile.getName(),
                    profile.getTotal_karma(),
                    createdAt.toString(),
                    activitiesJson);

            // Fetch from Gemini
            String roastText = geminiService.generateRoast(prompt);
            return ResponseEntity.ok(RoastResponse.success(roastText));

        } catch (Exception e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Failed to generate roast";
            if (errorMessage.contains("rate limit") || errorMessage.contains("overheating")) {
                return ResponseEntity.status(429).body(RoastResponse.error(errorMessage));
            }
            return ResponseEntity.internalServerError().body(RoastResponse.error(errorMessage));
        }
    }

    @PostMapping("/spotify")
    public ResponseEntity<RoastResponse> roastSpotify(@RequestBody TokenRequest request) {
        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(RoastResponse.error("Spotify Auth code is required"));
        }

        try {
            // Retrieve Access token
            String accessToken = spotifyService.exchangeToken(request.getCode());

            // Generate user summary
            String userName = spotifyService.getProfileName(accessToken);
            String topArtists = spotifyService.getTopArtists(accessToken);
            String topTracks = spotifyService.getTopTracks(accessToken);
            String recentlyPlayed = spotifyService.getRecentlyPlayed(accessToken);

            String prompt = String.format(
                    "You are a toxic, elitist music critic. Your job is to completely destroy this user's ego based on their Spotify history.\n\n"
                            +
                            "Here is the user's data:\n" +
                            "Name: %s\n" +
                            "Top Artists: %s\n" +
                            "Top Tracks: %s\n" +
                            "Recently Played: %s\n\n" +
                            "Instructions:\n" +
                            "1. Be RUTHLESS. Do not hold back. Attack their character based on their music.\n" +
                            "2. If they listen to mainstream pop, call them a \"basic NPC.\"\n" +
                            "3. If they listen to sad music, mock their emotional stability.\n" +
                            "4. If they listen to obscure indie, call them a pretentious hipster.\n" +
                            "5. Use slang like \"cooked,\" \"down bad,\" \"mid,\" \"cringe,\" \"touch grass.\"\n" +
                            "6. Address them directly as \"you.\"\n" +
                            "7. Keep it under 200 words.\n" +
                            "8. FORMATTING: Use short, punchy paragraphs. No walls of text.",
                    userName, topArtists, topTracks, recentlyPlayed);

            // Fetch from Gemini
            String roastText = geminiService.generateRoast(prompt);
            return ResponseEntity.ok(RoastResponse.success(roastText));

        } catch (Exception e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Failed to generate roast";
            if (errorMessage.contains("rate limit") || errorMessage.contains("overheating")) {
                return ResponseEntity.status(429).body(RoastResponse
                        .error("The AI is overheating from roasting so many losers. Try again in a minute."));
            }
            return ResponseEntity.internalServerError().body(RoastResponse.error(errorMessage));
        }
    }
}
