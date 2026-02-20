package com.cookedv2.cookedv2springboot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RedditActivity {
    private String subreddit;
    private String body; // for comments
    private String title; // for posts
    private int score;
}
