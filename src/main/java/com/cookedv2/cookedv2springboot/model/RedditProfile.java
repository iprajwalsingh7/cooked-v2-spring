package com.cookedv2.cookedv2springboot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RedditProfile {
    private String name;
    private long total_karma;
    private double created_utc;
}
