package com.example.BigLogger.src.Handler;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class MessagesRouteHandler implements RouteHandler {

    @Override
    public String getKey() {
        return "messages";
    }

    @Override
    @Async("taskExecutor")
    public CompletableFuture<ResponseEntity<?>> handle(HttpMethod method, Object body) {
        if (method == HttpMethod.GET) {
            return CompletableFuture.completedFuture((ResponseEntity<List<String>>) List.of("", ""));
        } else if (method == HttpMethod.POST) {
            return CompletableFuture.completedFuture((ResponseEntity<List<String>>) List.of("POST Messages Response: " + body));
        } else if (method == HttpMethod.PUT) {
            return CompletableFuture.completedFuture((ResponseEntity<List<String>>) List.of("PUT Messages Response: " + body));
        } else if (method == HttpMethod.DELETE) {
            return CompletableFuture.completedFuture((ResponseEntity<List<String>>) List.of("DELETE Messages Response"));
        } else {
            return CompletableFuture.completedFuture((ResponseEntity<List<String>>) List.of("Unsupported Method"));
        }
    }
}