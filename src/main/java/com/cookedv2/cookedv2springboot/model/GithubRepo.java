package com.cookedv2.cookedv2springboot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubRepo {
    private String name;
    private String language;
    private int stargazers_count;
    private int forks_count;
    private String updated_at;
    private String description;
}
