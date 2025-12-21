package com.example.BigLogger.src;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DBmanager {

    private static DBmanager instance = null;
    private HikariDataSource dataSource;

    private DBmanager() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("MySQL JDBC Driver load success");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(
                "jdbc:mysql://localhost:3306/biglog_conv" +
                        "?useSSL=false" +
                        "&serverTimezone=UTC" +
                        "&characterEncoding=UTF-8" +
                        "&rewriteBatchedStatements=true"
        );
        config.setUsername("root");
        config.setPassword("113456");

        // 커넥션 풀 옵션
        config.setMaximumPoolSize(10);      // 최대 커넥션 수
        config.setMinimumIdle(5);           // 유지할 idle 커넥션 수
        config.setIdleTimeout(600000);      // idle 유지 시간 (ms)
        config.setMaxLifetime(1800000);     // 커넥션 최대 생명 주기 (ms)
        config.setConnectionTimeout(30000); // 커넥션 요청 timeout (ms)

        dataSource = new HikariDataSource(config);
        System.out.println("HikariCP pool initialized");
    }

    public static DBmanager getInstance() {
        if (instance == null) {
            synchronized (DBmanager.class) {
                if (instance == null) {
                    instance = new DBmanager();
                }
            }
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        // 여기서 Connection은 새로 만드는 것이 아니라 Pool에서 빌려옴
        return dataSource.getConnection();
    }
}