package com.example.BigLogger.src.Handler;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface RouteHandler {
    String getKey();
    CompletableFuture<ResponseEntity<?>> handle(HttpMethod method, Object body);
}