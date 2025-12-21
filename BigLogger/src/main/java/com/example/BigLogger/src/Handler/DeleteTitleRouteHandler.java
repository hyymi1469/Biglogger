package com.example.BigLogger.src.Handler;

import com.example.BigLogger.src.DBmanager;
import com.example.BigLogger.src.LogTableInfo;
import com.example.BigLogger.src.Packet.ReqDeleteTitle;
import com.example.BigLogger.src.Packet.ReqTlogData;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;

@Service
public class DeleteTitleRouteHandler implements RouteHandler {

    private final Executor dbExecutor;

    public DeleteTitleRouteHandler(@Qualifier("dbExecutor") Executor dbExecutor) {
        this.dbExecutor = dbExecutor;
    }

    @Autowired
    private ObjectMapper mapper;

    @Override
    public String getKey() {
        return "deleteTitle";
    }

    @Override
    @Async("taskExecutor")
    public CompletableFuture<ResponseEntity<?>> handle(HttpMethod method, Object body) {

        if (method != HttpMethod.POST) {
            return CompletableFuture.completedFuture(
                    ResponseEntity.badRequest().body(
                            Map.of(
                                    "success", false,
                                    "message", "잘못된 요청입니다."
                            )
                    )
            );
        }

        // 파싱
        ReqDeleteTitle req = mapper.convertValue(body, ReqDeleteTitle.class);

        return CompletableFuture.supplyAsync(() -> {

            String tableName = req.Title;

            if (tableName == null || tableName.isBlank()) {
                return ResponseEntity.badRequest().body("Invalid table name");
            }

            // 테이블명 SQL Injection 방지: 백틱 처리 & 위험 문자 제거
            //tableName = tableName.replaceAll("[^a-zA-Z0-9_]", "");

            String sql = "DROP TABLE IF EXISTS `" + tableName + "`";

            try (Connection conn = DBmanager.getInstance().getConnection();
                 Statement stmt = conn.createStatement()) {

                stmt.execute(sql);

                // LogTableInfo에서도 제거
                LogTableInfo.getInstance().GetLogNameMap().remove(tableName);

                return ResponseEntity.ok(
                        Map.of(
                                "success", true,
                                "message", "Deleted table: " + tableName
                        )
                );

            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.internalServerError().body(
                        Map.of(
                                "success", false,
                                "message", "Failed to delete table: " + tableName
                        )
                );
            }

        }, dbExecutor);

    }

}
