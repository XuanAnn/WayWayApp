package com.example.waywaybackend.common;

public record ApiError(
        String error,
        String message
) {
}

