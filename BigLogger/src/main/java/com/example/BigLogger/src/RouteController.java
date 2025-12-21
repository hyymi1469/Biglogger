package com.example.BigLogger.src;


import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import com.example.BigLogger.src.Handler.MessagesRouteHandler;

@RestController
@RequestMapping("/api")
public class RouteController {

    private final MessageService messageService;

    public RouteController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/{key}")
    public CompletableFuture<ResponseEntity<?>> getRoute(@PathVariable String key) {
        return messageService.handle(key, HttpMethod.GET, null);
    }

    @PostMapping("/{key}")
    public CompletableFuture<ResponseEntity<?>> postRoute(@PathVariable String key, @RequestBody Object body) {
        return messageService.handle(key, HttpMethod.POST, body);
    }

    @PutMapping("/{key}")
    public CompletableFuture<ResponseEntity<?>> putRoute(@PathVariable String key, @RequestBody Object body) {
        return messageService.handle(key, HttpMethod.PUT, body);
    }

    @DeleteMapping("/{key}")
    public CompletableFuture<ResponseEntity<?>> deleteRoute(@PathVariable String key) {
        return messageService.handle(key, HttpMethod.DELETE, null);
    }
}