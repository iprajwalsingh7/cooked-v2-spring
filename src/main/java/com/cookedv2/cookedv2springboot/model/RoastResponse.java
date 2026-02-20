package com.cookedv2.cookedv2springboot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoastResponse {
    private String roast;
    private String error;

    public static RoastResponse success(String roast) {
        return new RoastResponse(roast, null);
    }

    public static RoastResponse error(String error) {
        return new RoastResponse(null, error);
    }
}
