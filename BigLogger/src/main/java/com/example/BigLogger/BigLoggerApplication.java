package com.example.BigLogger;

import com.example.BigLogger.src.DBmanager;
import com.example.BigLogger.src.LogTableInfo;
import com.example.BigLogger.src.XMLManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CopyOnWriteArrayList;

@SpringBootApplication
public class BigLoggerApplication {

	public static void main(String[] args) {

        // XML불러오기
        if(!XMLManager.getInstance().LoadXml())
            return;

        // DB콜
        try (Connection conn = DBmanager.getInstance().getConnection()) {

            String sql = "SELECT table_name FROM information_schema.tables WHERE table_schema = DATABASE()";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String tableName = rs.getString("table_name");
                    LogTableInfo info = LogTableInfo.getInstance();
                    info.GetLogNameMap().put(tableName, new CopyOnWriteArrayList<>());
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

		SpringApplication.run(BigLoggerApplication.class, args);
	}
}
