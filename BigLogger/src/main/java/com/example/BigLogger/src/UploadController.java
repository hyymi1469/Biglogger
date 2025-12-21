package com.example.BigLogger.src;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.context.request.async.DeferredResult;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.zip.GZIPOutputStream;
import java.util.concurrent.Executor;

@RestController
public class UploadController {

    private final Executor dbExecutor;

    public UploadController(@Qualifier("dbExecutor") Executor dbExecutor) {
        this.dbExecutor = dbExecutor;
    }

    @PostMapping("/api/upload")
    public DeferredResult<ResponseEntity<String>> upload(@RequestParam("file") MultipartFile file) {

        // 코루틴이 없으니 DB스레드에서 일할 때 거기서 응답시키기
        // 타임아웃 지정
        DeferredResult<ResponseEntity<String>> deferred =
                new DeferredResult<>(600_000L); // 10분

        // 타임아웃 발생 시 동작 등록
        deferred.onTimeout(() -> {
            deferred.setErrorResult(
                    ResponseEntity
                            .status(503)
                            .body("upload processing timeout")
            );
        });

        if (file.isEmpty()) {
            deferred.setErrorResult(
                    ResponseEntity.badRequest().body("file upload failed.")
            );
            return deferred;
        }

        // 1. 테이블 존재 여부 확인
        String tableName = file.getOriginalFilename();
        System.out.println("upload file name: " + tableName);

        LogTableInfo tlogInfo = LogTableInfo.getInstance();
        if(tlogInfo.GetLogNameMap().containsKey(tableName))
        {
            deferred.setResult(
                    ResponseEntity.badRequest().body("이미 업로드된 파일명입니다: " + tableName)
            );
            return deferred;
        }

        // DB스레드서 실행
        CompletableFuture.runAsync(() -> {

            try (Connection conn = DBmanager.getInstance().getConnection()) {

                // 트랜잭션 시작
                conn.setAutoCommit(false);

                // 1. 테이블 생성
                String createSql = "CREATE TABLE `" + tableName + "` (" +
                        "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                        "table_name VARCHAR(128) NOT NULL, " +
                        "data LONGBLOB NOT NULL, " +
                        "INDEX idx_extra (table_name)" +
                        ")";

                try (PreparedStatement createStmt = conn.prepareStatement(createSql)) {
                    createStmt.executeUpdate();
                }

                // 2. INSERT 준비
                String insertSql =
                        "INSERT INTO `" + tableName + "` (table_name, data) VALUES (?, ?)";

                Set<String> tableNameSet = new HashSet<>();

                try (
                        PreparedStatement insertStmt = conn.prepareStatement(insertSql);
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))
                ) {
                    String line;
                    int batchCount = 0;

                    while ((line = reader.readLine()) != null) {

                        String title = line.split("\\|")[0];

                        if(!XMLManager.getInstance().getStructMap().containsKey(title))
                        {
                            continue;
                        }

                        tableNameSet.add(title);

                        // gzip 압축
                        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                        try (GZIPOutputStream gzip = new GZIPOutputStream(byteStream)) {
                            gzip.write(line.getBytes(StandardCharsets.UTF_8));
                        }

                        insertStmt.setString(1, title);
                        insertStmt.setBytes(2, byteStream.toByteArray());
                        insertStmt.addBatch();

                        // batch 실행
                        if (++batchCount % 1000 == 0) {
                            insertStmt.executeBatch();
                            insertStmt.clearBatch();
                        }
                    }

                    // 마지막 남은 batch 처리
                    insertStmt.executeBatch();
                }

                // 커밋
                conn.commit();

                CopyOnWriteArrayList<String> tableNameList =
                        new CopyOnWriteArrayList<>(tableNameSet);
                Collections.sort(tableNameList);
                tlogInfo.GetLogNameMap().put(tableName, tableNameList);

                if (!deferred.isSetOrExpired()) {
                    deferred.setResult(ResponseEntity.ok("upload completed"));
                }

            } catch (Exception e) {

                e.printStackTrace();

                if (!deferred.isSetOrExpired()) {
                    deferred.setErrorResult(
                            ResponseEntity.internalServerError().body("file upload failed.")
                    );
                }
            }

        }, dbExecutor);

        return deferred;
    }
}