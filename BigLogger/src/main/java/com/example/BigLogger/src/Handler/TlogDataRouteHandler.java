package com.example.BigLogger.src.Handler;

import com.example.BigLogger.src.DBmanager;
import com.example.BigLogger.src.Packet.ReqTlogData;
import com.example.BigLogger.src.LogTableInfo;
import com.example.BigLogger.src.XMLManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.zip.GZIPInputStream;

@Service
public class TlogDataRouteHandler implements RouteHandler {

    @Autowired
    private ObjectMapper mapper;

    @Override
    public String getKey() {
        return "tlogData";
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
        ReqTlogData req = mapper.convertValue(body, ReqTlogData.class);

        ConcurrentHashMap<String, CopyOnWriteArrayList<String>> logMap = LogTableInfo.getInstance().GetLogNameMap();
        CopyOnWriteArrayList<String> tableNameList = logMap.get(req.Title);

        // 비어있다면 DB 불러서 데이터 넣어줌(메모리 캐싱)
        if (tableNameList.isEmpty()) {
            Set<String> tableNameSet = new HashSet<>();
            try (Connection conn = DBmanager.getInstance().getConnection()) {

                String sql = "SELECT data FROM `" + req.Title + "` ORDER BY id ASC";
                try (PreparedStatement stmt = conn.prepareStatement(sql);
                     ResultSet rs = stmt.executeQuery()) {

                    while (rs.next()) {
                        byte[] compressed = rs.getBytes("data");

                        // GZIP 압축 해제
                        ByteArrayInputStream bais = new ByteArrayInputStream(compressed);
                        GZIPInputStream gzip = new GZIPInputStream(bais);
                        BufferedReader br = new BufferedReader(new InputStreamReader(gzip, StandardCharsets.UTF_8));

                        String line = br.readLine();  // 한 줄씩 들어있음
                        if (line != null) {
                            String title = line.split("\\|")[0];

                            if(!XMLManager.getInstance().getStructMap().containsKey(title))
                            {
                                continue;
                            }

                            tableNameSet.add(title);
                        }

                        br.close();
                        gzip.close();
                        bais.close();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                return CompletableFuture.completedFuture(
                        ResponseEntity.internalServerError().body(
                                Map.of(
                                        "success", false,
                                        "message", "DB 조회 실패"
                                )
                        )
                );
            }

            tableNameList = new CopyOnWriteArrayList<>(tableNameSet);
            Collections.sort(tableNameList);
            logMap.put(req.Title, tableNameList);
        }

        return CompletableFuture.completedFuture(
                ResponseEntity.ok(
                        Map.of(
                                "Title", req.Title,
                                "TitleList", tableNameList
                        )
                )
        );
    }
}
