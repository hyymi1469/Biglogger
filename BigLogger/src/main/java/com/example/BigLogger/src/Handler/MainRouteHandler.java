package com.example.BigLogger.src.Handler;

import com.example.BigLogger.src.LogTableInfo;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class MainRouteHandler implements RouteHandler {

    @Override
    public String getKey() {
        return "main";
    }

    @Override
    @Async("taskExecutor")
    public CompletableFuture<ResponseEntity<?>> handle(HttpMethod method, Object body) {

        if (method != HttpMethod.GET) {
            return CompletableFuture.completedFuture(
                    ResponseEntity.badRequest().body(
                            Map.of(
                                    "success", false,
                                    "message", "잘못된 요청입니다."
                            )
                    )
            );
        }

        ConcurrentHashMap<String, CopyOnWriteArrayList<String>> tableMap = LogTableInfo.getInstance().GetLogNameMap();
        List<Map<String, Object>> list = new ArrayList<>();

        int index = 0;
        for (String tableName : tableMap.keySet()) {
            list.add(Map.of(
                    "Titile", tableName,  // 오타 intentionally 그대로
                    "Index", index++
            ));
        }

        return CompletableFuture.completedFuture(
                ResponseEntity.ok(
                        Map.of(
                                "tables", list
                        )
                )
        );
    }
}