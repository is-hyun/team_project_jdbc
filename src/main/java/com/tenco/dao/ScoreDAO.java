package com.tenco.dao;

import com.tenco.dto.Lectures;
import com.tenco.dto.Members;
import com.tenco.dto.Scores;
import com.tenco.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ScoreDAO {

    // 모든 성적 조회
    public List<Scores> getAllScores() {
        List<Scores> scoreList = new ArrayList<>();
        String sql = """
                select s.id, m.member_id, m.name, l.lecture_code, l.lecture_name, s.score
                from scores s
                join members m
                on s.member_id = m.id
                join lectures l
                on s.lecture_id = l.id;
                """;

        try (Connection connection = DatabaseUtil.getConnection()) {

            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    // 자료구조에 생성된 Student 객체를 하나씩 추가 함
                    Scores score = createScore(rs);
                    scoreList.add(score);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return scoreList;
    }

    // 본인 성적 조회
    public List<Scores> getScoresById(String memberId) {
        List<Scores> scoreList = new ArrayList<>();
        String sql = """
                select s.id, m.member_id, m.name, l.lecture_code, l.lecture_name, s.score
                from scores s
                join members m
                on s.member_id = m.id
                join lectures l
                on s.lecture_id = l.id
                where m.member_id = ?;
                """;

        try (Connection connection = DatabaseUtil.getConnection()) {

            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, memberId);
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    // 자료구조에 생성된 Student 객체를 하나씩 추가 함
                    Scores score = createScore(rs);
                    scoreList.add(score);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return scoreList;
    }

    // 성적 수정
    public void updateScore(Members member, Lectures lecture, Integer score) throws SQLException {
        Connection conn = DatabaseUtil.getConnection();

        String sql = """
                update scores
                set score = ?
                where member_id = ?
                and lecture_id = ?
                """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, score);
            pstmt.setInt(2, member.getId());
            pstmt.setInt(3, lecture.getId());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException(e);
        } finally {
            conn.close();
        }

    }

    // 성적 추가
    // [처리 순서]
    // 1. DB 연결을 얻고 자동 커밋을 끈다 (트랜잭션 시작)
    // 2. 입력받은 학생과 동일한 학생 있는지 검색한다 -- select
    // 3. 입력받은 과목과 동일한 과목이 있는지 검색한다 -- select
    // 4. 위에 입력받은 학생과 과목 아이디로 registration 테이블에 데이터가 있지 않으면
    //      rollback를 한다 -- insert
    // 5. 2 - 4번이 모두 성공하면 commit, 하나라도 실패하면 rollback
    // 6. 자동 커밋을 원래대로 되돌리고 연결을 닫는다

    // 현재 학생 객체와 과목 객체를 받을 방법이 없음
    public void addScore(Members member, Lectures lecture) throws SQLException {
        Connection conn = null;

        try {
            conn = DatabaseUtil.getConnection();

            conn.setAutoCommit(false);

            String checkSql = """
                    select *
                    from registration
                    where member_id = ?
                    and lecture_id = ?
                    """;
            try (PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
                pstmt.setInt(1, member.getId());
                pstmt.setInt(2, lecture.getId());

                ResultSet rs = pstmt.executeQuery();

                if (!rs.next()) {
                    throw new SQLException("해당 과목을 수강하신 기록이 없습니다");
                }
            }

            String insertSql = """
                    insert into scores(member_id, lecture_id)
                    values (?, ?)
                    """;
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setInt(1, member.getId());
                pstmt.setInt(2, lecture.getId());

                pstmt.executeUpdate();
            }
            conn.commit();

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackException){
                    e.addSuppressed(rollbackException);
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    // 성적 삭제
    // [처리 순서]
    // 1. DB 연결을 얻고 자동 커밋을 끈다 (트랜잭션 시작)
    // 2. 입력받은 학생과 동일한 학생 있는지 검색한다 -- select
    // 3. 입력받은 과목과 동일한 과목이 있는지 검색한다 -- select
    // 4. 위에 입력받은 학생과 과목 아이디로 registration 테이블에 데이터가 있지 않으면
    //      rollback를 한다 -- delete
    // 5. 2 - 4번이 모두 성공하면 commit, 하나라도 실패하면 rollback
    // 6. 자동 커밋을 원래대로 되돌리고 연결을 닫는다
    public void deleteScore(Members member, Lectures lecture) {
        try (Connection conn = DatabaseUtil.getConnection()) {

            String sql = """
                    delete from scores
                    where member_id = ?
                    and lecture_id = ?
                    """;
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, member.getId());
                pstmt.setInt(2, lecture.getId());

                pstmt.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // score 객체 생성
    private static Scores createScore(ResultSet rs) throws SQLException {
        return Scores.builder()
                .id(rs.getInt("id"))
                .memberId(rs.getString("member_id"))
                .name(rs.getString("name"))
                .lectureCode(rs.getString("lecture_code"))
                .lectureName(rs.getString("lecture_name"))
                .score(rs.getInt("score"))
                .build();
    }
}
