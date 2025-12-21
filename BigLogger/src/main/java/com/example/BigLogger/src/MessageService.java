package com.example.BigLogger.src;

import com.example.BigLogger.src.Handler.RouteHandler;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class MessageService {

    private final Map<String, RouteHandler> handlers;

    // Spring이 모든 RouteHandler Bean을 주입
    public MessageService(List<RouteHandler> handlerList) {
        this.handlers = handlerList.stream()
                .collect(Collectors.toMap(RouteHandler::getKey, h -> h));
    }

    public CompletableFuture<ResponseEntity<?>> handle(String key, HttpMethod method, Object body) {
        RouteHandler handler = handlers.get(key);
        if (handler == null) {
            return CompletableFuture.failedFuture(
                    new RuntimeException("No handler found for key: " + key)
            );
        }

        return handler.handle(method, body);
    }
}