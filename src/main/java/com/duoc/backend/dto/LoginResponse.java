package com.duoc.backend.dto;

public record LoginResponse(String token, String tokenType, String username, String rol) {
}
