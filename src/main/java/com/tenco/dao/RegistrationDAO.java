package com.tenco.dao;

import com.tenco.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RegistrationDAO {

    //수강 신청
    public void registerLecture(String memId, String lecId) {
        String rlSql = """
                INSERT INTO registration (member_id, lecture_id) 
                VALUES (?, ?)
                """;

        try (Connection conn = DatabaseUtil.getConnection()) {

            try (PreparedStatement pstmt = conn.prepareStatement(rlSql)) {
                pstmt.setString(1, memId);
                pstmt.setString(2, lecId);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


        //본인 수강신청 조회
        //전체 조회 (관리자)

    }
}
