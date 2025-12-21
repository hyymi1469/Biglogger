package com.example.BigLogger.src.Handler;

import com.example.BigLogger.src.DBmanager;
import com.example.BigLogger.src.LogEntry;
import com.example.BigLogger.src.LogStruct;
import com.example.BigLogger.src.Packet.ReqTlogData;
import com.example.BigLogger.src.Packet.ReqTlogTableData;
import com.example.BigLogger.src.XMLManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@Service
public class TlogTableDataRouteHandler implements RouteHandler {

    private final Executor dbExecutor;

    public TlogTableDataRouteHandler(@Qualifier("dbExecutor") Executor dbExecutor) {
        this.dbExecutor = dbExecutor;
    }

    @Autowired
    private ObjectMapper mapper;

    @Override
    public String getKey() {
        return "tlogTableData";
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

        ReqTlogTableData req = mapper.convertValue(body, ReqTlogTableData.class);

        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = DBmanager.getInstance().getConnection()) {

                String sql = "SELECT * FROM `" + req.Title + "` WHERE table_name = ?";

                // 최종 결과 담을 Map<String, List<String>>
                Map<String, List<String>> result = new HashMap<>();

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {

                    stmt.setString(1, req.TableName);

                    try (ResultSet rs = stmt.executeQuery()) {

                        while (rs.next()) {

                            byte[] data = rs.getBytes("data");

                            try (ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
                                 GZIPInputStream gzipStream = new GZIPInputStream(byteStream);
                                 InputStreamReader reader = new InputStreamReader(gzipStream, StandardCharsets.UTF_8);
                                 BufferedReader buffered = new BufferedReader(reader)) {

                                StringBuilder sb = new StringBuilder();
                                String line;

                                while ((line = buffered.readLine()) != null) {
                                    sb.append(line);
                                }

                                String logLine = sb.toString();

                                // XML 구조 가져오기
                                LogStruct struct = XMLManager.getInstance().getStruct(req.TableName);
                                if (struct == null) {
                                    return ResponseEntity.badRequest()
                                            .body("XML struct not found: " + req.TableName);
                                }

                                // | split
                                String[] parts = logLine.split("\\|");

                                // 매핑
                                Map<String, String> mapped = new LinkedHashMap<>();
                                int count = Math.min(parts.length, struct.entries.size());

                                for (int i = 0; i < count; i++) {
                                    LogEntry entry = struct.entries.get(i);
                                    if(entry == null || parts.length - 1 <= i)
                                        continue;

                                    mapped.put(entry.name, parts[i+1]);
                                }

                                // JSON 변환
                                String jsonStr = mapper.writeValueAsString(mapped);
                                result.computeIfAbsent(req.TableName, k -> new ArrayList<>())
                                        .add(jsonStr);
                            }
                        }
                    }
                }

                // 최종 JSON 응답
                return ResponseEntity.ok(result);

            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.internalServerError().body("DB error");
            }

        }, dbExecutor);

    }
}
