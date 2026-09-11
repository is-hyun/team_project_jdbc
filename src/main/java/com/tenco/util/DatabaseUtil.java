package com.tenco.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseUtil {

    private static final String URL =
            "jdbc:mysql://192.168.5.16:3306/project_team1?serverTimezone=Asia/Seoul";
    private static final String PJ_USER = "team1";
    private static final String PJ_PASSWORD = "pj1234";
    private static final HikariDataSource DATA_SOURCE;

    static {
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl(URL);
        config.setUsername(PJ_USER);
        config.setPassword(PJ_PASSWORD);

        config.setMaximumPoolSize(10);
        config.setMinimumIdle(5);

        config.setConnectionTimeout(3000);

        DATA_SOURCE = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        return DATA_SOURCE.getConnection();
    }

    public static void close(){
        if (!DATA_SOURCE.isClosed()){
            DATA_SOURCE.close();
        }
    }
}