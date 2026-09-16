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

            // 학생ID가 존재하는지 확인
            String memberChkSql = """
                    SELECT id FROM members WHERE id = ?
                    """;
            try (PreparedStatement memberPstmt = conn.prepareStatement(memberChkSql)) {
                memberPstmt.setString(1, memId);
                try (ResultSet rs = memberPstmt.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("존재하지 않는 회원입니다. 회원ID : " + memId);
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
                overRegPstmt.executeUpdate();

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

    // 수강 신청 취소

    // 수강 신청 취소
    public List<Registration> deleteRegistration(String memId, String lecId) throws SQLException {
        List<Registration> registrationList = new ArrayList<>();

        Connection conn = null;

        try {
            conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false);

            // 1. 맴버ID로 수강신청내역 확인
            String chkSql = """
                    select * from registration 
                    where member_id = ?
                    """;

            try (PreparedStatement checkPstmt = conn.prepareStatement(chkSql)) {
                checkPstmt.setString(1, memId);
                try (ResultSet rs = checkPstmt.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("본인으로 신청된 강의가 없습니다 ID : " + memId);
                    }
                }
            }

            // 2. 취소 하고싶은 수강ID 삭제
            String deleteSql = """
                    delete from registration 
                    where member_id = ? and lecture_id = ?
                    """;
            String selectLectureSql = """
                    select available 
                    from lectures 
                    where id = ?
                    """;
            String updateSql = """
                    update lectures set available = true 
                    where id = ?
                    """;

            try (PreparedStatement deletePstmt = conn.prepareStatement(deleteSql)) {
                deletePstmt.setString(1, memId);
                deletePstmt.setString(2, lecId);
                int rows = deletePstmt.executeUpdate();
                if (rows == 0) {
                    throw new SQLException("존재하지 않는 강의입니다. ID : " + lecId);
                }

                // 삭제 후 lectures 조회해서 available false라면 true로 변경
                boolean isAvailable = true;
                try (PreparedStatement selectLecturePstmt = conn.prepareStatement(selectLectureSql)) {
                    selectLecturePstmt.setString(1, lecId);
                    try (ResultSet rs = selectLecturePstmt.executeQuery()) {
                        if (rs.next()) {
                            isAvailable = rs.getBoolean("available");
                        }
                    }
                }

                if (!isAvailable) {
                    try (PreparedStatement updatePstmt = conn.prepareStatement(updateSql)) {
                        updatePstmt.setString(1, lecId);
                        updatePstmt.executeUpdate();
                    }
                }
            }

            // 모두 성공시 commit
            conn.commit();

        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            // 👉 e.getMessage()를 포함해서 던지도록 수정합니다!
            e.printStackTrace();
            throw new RuntimeException("수강 취소 실패: " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return registrationList;
    }


    //본인 수강신청 조회
    public List<Registration> getMyRegistrations(String studentId) {
        List<Registration> registrationList = new ArrayList<>();

        String searchSql = """
                select m.member_id, m.name, r.lecture_id, l.lecture_code, l.lecture_name, l.professor, l.credit
                from registration r
                join members m on r.member_id = m.id
                join lectures l on r.lecture_id = l.id
                where m.member_id = ?
                """;

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(searchSql)) {

            pstmt.setString(1, studentId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    registrationList.add(Registration.builder()
                            .memberId(rs.getString("member_id"))
                            .memberName(rs.getString("name"))
                            .lectureId(rs.getInt("lecture_id"))
                            .lectureCode(rs.getString("lecture_code"))
                            .lectureName(rs.getString("lecture_name"))
                            .professor(rs.getString("professor"))
                            .credit(rs.getInt("credit"))
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
                select r.id, r.member_id, m.name, r.lecture_id, l.lecture_name
                from registration r
                join members m on r.member_id = m.id
                join lectures l on r.lecture_id = l.id
                order by r.member_id
                """;

        if (members == null || !members.isAdmin()) {
            return new ArrayList<>();
        }

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(searchAllSql)) {


            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    registrationList.add(Registration.builder()
                            .id(rs.getInt("id"))
                            .memberId(rs.getString("member_id"))
                            .memberName(rs.getString("name"))
                            .lectureId(rs.getInt("lecture_id"))
                            .lectureName(rs.getString("lecture_name"))
                            .build());
                }
            }
        } catch (SQLException e) {
            System.err.println("조회 실패 : " + e);
        }


        return registrationList;
    }

}
