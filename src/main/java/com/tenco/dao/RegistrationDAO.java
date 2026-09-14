package com.tenco.dao;

import com.tenco.dto.Members;
import com.tenco.dto.Registration;
import com.tenco.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RegistrationDAO {

    //수강 신청
    public void registerLecture(String memId, String lecId) throws SQLException {
        // [처리 순서]

        // 1. DB 연결을 얻고 자동 커밋을 끈다
        Connection conn = null;
        try {
            conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false);

            // 2. 강의가 존재하고 수강신청이 가능한 상태인지 확인 -- SELECT (정원초과 상태 필요해 보임)
            String chkSql = """
                    SELECT available FROM lectures WHERE id = ?
                    """;
            try (PreparedStatement checkPstmt = conn.prepareStatement(chkSql)) {
                checkPstmt.setString(1, lecId);
                try (ResultSet rs = checkPstmt.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("존재하지 않는 강의입니다. 강의ID : " + lecId);
                    }
                    if (!rs.getBoolean("available")) {
                        throw new SQLException("현재 정원이 초과 되었습니다.");
                    }
                }
            }

            // 3. 수강 신청 -- INSERT
            String regSql = """
                    INSERT INTO registration (member_id, lecture_id)
                    VALUES (?, ?)
                    """;
            try (PreparedStatement regPstmt = conn.prepareStatement(regSql)) {
                regPstmt.setString(1, memId);
                regPstmt.setString(2, lecId);

                regPstmt.executeUpdate();
            }

            // 4. 정원초과시 수강신청 불가능 상태로 변경 -- UPDATE
            String overRegSql = """
                    UPDATE lectures l
                    SET l.available = false
                    WHERE l.id = ?
                    AND (SELECT COUNT(*) FROM registration r WHERE r.lecture_id = l.id) >= l.capacity
                    """;
            try (PreparedStatement overRegPstmt = conn.prepareStatement(overRegSql)) {

                overRegPstmt.setString(1, lecId);

            }
            // 5. 2 ~ 4 이 모두 성공하면 commit, 하나라도 실패하면 rollback
            conn.commit();

            // 6. 자동 커밋을 원래대로 되돌리고 연결을 닫는다.

        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true); // 다시 변경 반드시 처리
                conn.close();
            }
        }


    }

    //본인 수강신청 조회
    public List<Registration> getMyRegistrations(int studentId) {
        List<Registration> registrationList = new ArrayList<>();

        String searchSql = """
                select id, member_id, lecture_id 
                from registration 
                where member_id = ?
                """;

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(searchSql)) {

            pstmt.setInt(1, studentId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    registrationList.add(Registration.builder()
                            .id(rs.getInt("id"))
                            .memberId(rs.getInt("member_id"))
                            .lectureId(rs.getInt("lecture_id"))
                            .build());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("학생 등록 정보를 조회하는 중 오류가 발생했습니다");
        }

        return registrationList;
    }


    //전체 조회 (관리자)
    public List<Registration> getAllRegistrations(Members members) {
        List<Registration> registrationList = new ArrayList<>();

        String searchAllSql = """
                select r.id, r.member_id, r.lecture_id, m.name as member_name
                from registration r
                join members m on r.member_id = m.id
                """;

        if (members == null || !members.isAdmin()) {
            System.out.println("관리자만 조회 가능 합니다.");
            return new ArrayList<>();
        }

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(searchAllSql)) {


            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    registrationList.add(Registration.builder()
                            .id(rs.getInt("id"))
                            .memberId(rs.getInt("member_id"))
                            .memberName(rs.getString("member_name"))
                            .lectureId(rs.getInt("lecture_id"))
                            .build());
                }
            }
        } catch (SQLException e) {
        }


        return registrationList;
    }

}


