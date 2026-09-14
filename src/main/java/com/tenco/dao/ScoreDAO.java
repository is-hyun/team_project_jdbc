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
                where s.member_id = ?;
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
    // [처리 순서]
    // 1. DB 연결을 얻고 자동 커밋을 끈다 (트랜잭션 시작)
    // 2. 이 학생의 성적목록을 출력 -- select
    // 3. 찾은 성적을 score로 수정한다 -- update
    // 4. 2 - 3번이 모두 성공하면 commit, 하나라도 실패하면 rollback
    // 5. 자동 커밋을 원래대로 되돌리고 연결을 닫는다
    public void updateScore(Members member, Lectures lecture, Integer score) throws SQLException{
        Connection conn = null;

        try {
            conn = DatabaseUtil.getConnection();

            conn.setAutoCommit(false);

            String checkSql = """
                    select *
                    from scores
                    where member_id = ?
                    and lecture_id = ?
                    """;

            int updateLecture;

            try (PreparedStatement checkPstmt = conn.prepareStatement(checkSql)) {
                checkPstmt.setInt(1, member.getId());
                checkPstmt.setInt(2, lecture.getId());

                try (ResultSet rs = checkPstmt.executeQuery()) {
                    if (!rs.next()){
                        throw new SQLException("해당 과목의 수강 기록이 없습니다");
                    }
                    updateLecture = rs.getInt("id");
                }
            }

            String updateSql = """
                    update scores
                    set score = ?
                    where id = ?
                    """;

            try (PreparedStatement updatePstmt = conn.prepareStatement(updateSql)) {
                updatePstmt.setInt(1, score);
                updatePstmt.setInt(2, updateLecture);

                updatePstmt.executeUpdate();
            }

            conn.commit();

        } catch (SQLException e) {
            if (conn != null){
                conn.rollback();
            }
            throw new RuntimeException(e);
        }finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
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

        try (Connection conn = DatabaseUtil.getConnection()) {

            String sql = """
                    insert into scores(member_id, lecture_id)
                    values (?, ?)
                    """;
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, member.getId());
                pstmt.setInt(2, lecture.getId());
            }

        } catch (SQLException e){
            throw new RuntimeException(e);
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
    public void deleteScore(Members member, Lectures lecture){
        try (Connection conn = DatabaseUtil.getConnection()) {

            String sql = """
                    delete from scores
                    where member_id = ?
                    and lecture_id = ?
                    """;
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, member.getId());
                pstmt.setInt(2, lecture.getId());
            }

        } catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

    // score 객체 생성
    private static Scores createScore(ResultSet rs) throws SQLException {
        Scores scores = Scores.builder()
                .id(rs.getInt("id"))
                .memberId(rs.getString("member_id"))
                .name(rs.getString("name"))
                .lectureId(rs.getString("lecture_code"))
                .lectureName(rs.getString("lecture_name"))
                .score(rs.getInt("score"))
                .build();
        return scores;
    }
}
