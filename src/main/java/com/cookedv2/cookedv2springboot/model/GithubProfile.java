package com.cookedv2.cookedv2springboot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubProfile {
    private String login;
    private String bio;
    private int followers;
    private int public_repos;
    private String created_at;
}
