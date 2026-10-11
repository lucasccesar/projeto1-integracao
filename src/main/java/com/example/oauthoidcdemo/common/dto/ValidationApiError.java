package com.example.oauthoidcdemo.common.dto;

import java.time.Instant;
import java.util.Map;

public record ValidationApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors
) {
}
