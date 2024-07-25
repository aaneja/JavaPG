package org.example;

import com.google.common.base.Stopwatch;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DuckDbBulkInsert {

    private static final String DB_URL = "jdbc:duckdb:/home/anant/temp/insert_test.duckdb";
    private static final String CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS users (id1 INTEGER, id2 INTEGER, id3 INTEGER, id4 INTEGER, id5 INTEGER, " +
            " name1 VARCHAR, name2 VARCHAR, name3 VARCHAR, name4 VARCHAR, name5 VARCHAR)";
    private static final String INSERT_SQL = "INSERT INTO users (id1, id2, id3, id4, id5, name1, name2, name3, name4, name5) " +
            "VALUES (?, ?, ?, ? , ?, ? , ? , ?, ?, ?)";

    public static void insertTest(long recordCount) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement createTableStmt = conn.prepareStatement(CREATE_TABLE_SQL)) {
            // Create table if not exists
            createTableStmt.execute();

            Stopwatch sw;
            try (PreparedStatement insertStmt = conn.prepareStatement(INSERT_SQL)) {

                // Begin transaction
                conn.setAutoCommit(false);

                // Insert records in bulk
                sw = Stopwatch.createStarted();
                for (int i = 1; i <= recordCount; i++) {
                    insertStmt.setInt(1, i);
                    insertStmt.setInt(2, i+1);
                    insertStmt.setInt(3, i+2);
                    insertStmt.setInt(4, i+3);
                    insertStmt.setInt(5, i+4);
                    insertStmt.setString(6, "User" + i);
                    insertStmt.setString(7, "User" + i);
                    insertStmt.setString(8, "User" + i);
                    insertStmt.setString(9, "User" + i);
                    insertStmt.setString(10, "User" + i);
                    insertStmt.addBatch();
                }

                // Execute batch
                insertStmt.executeBatch();
            }

            // Commit transaction
            conn.commit();
            sw.stop();

            System.out.printf("[%d] Records inserted successfully, took [%d] ms%n", recordCount, sw.elapsed().toMillis());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}