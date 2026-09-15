package com.tenco.service;

import com.tenco.dao.LecturesDAO;
import com.tenco.dao.MembersDAO;
import com.tenco.dao.ScoreDAO;
import com.tenco.dto.Lectures;
import com.tenco.dto.Members;
import com.tenco.dto.Scores;

import java.sql.SQLException;
import java.util.List;

public class ScoreService {

    private LecturesDAO lecturesDAO = new LecturesDAO();
    private MembersDAO membersDAO = new MembersDAO();
    private ScoreDAO scoreDAO = new ScoreDAO();

    ///////////////////////////////////////////////////////
    // Score 관련 기능
    ///////////////////////////////////////////////////////

    // 성적 전체 조회 (관리자 전용)
    public List<Scores> getAllScores() throws SQLException{
        return scoreDAO.getAllScores();
    }

    // 본인 성적 조회 (멤버 로그인 대상 전용)
    public List<Scores> getScoresById(String memberId) throws SQLException{
        // 방어적 코드
        if (memberId == null || memberId.trim().isEmpty()) {
            throw new SQLException("회원 ID가 올바르지 않습니다.");
        }

        return scoreDAO.getScoresById(memberId);
    }

    // 성적 수정
    public void updateScore(String memberName, String lectureName, Integer score) throws SQLException {
        if (memberName == null || memberName.trim().isEmpty() ||
                lectureName == null || lectureName.trim().isEmpty()){
            throw new SQLException("이름과 과목을 제대로 입력해주세요!");
        }

        if (score == null || score < 0 || score > 100){
            throw new SQLException("성적을 제대로 입력해주세요");
        }

        Members member = membersDAO.searchMembersByMemberId(memberName);
        if (member == null) {
            throw new SQLException("해당 아이디의 학생이 없습니다.");
        }
        Lectures lecture = lecturesDAO.getLectureByFullname(lectureName);
        if (lecture == null) {
            throw new SQLException("해당 이름의 강의가 없습니다.");
        }

        scoreDAO.updateScore(member, lecture, score);
    }

    // 성적 추가
    public void addScore(String memberName, String lectureName) throws SQLException {
        if (memberName == null || memberName.trim().isEmpty() ||
                lectureName == null || lectureName.trim().isEmpty()){
            throw new SQLException("이름과 과목을 제대로 입력해주세요!");
        }

        Members member = membersDAO.searchMembersByMemberId(memberName);
        if (member == null) {
            throw new SQLException("해당 아이디의 학생이 없습니다.");
        }
        Lectures lecture = lecturesDAO.getLectureByFullname(lectureName);
        if (lecture == null) {
            throw new SQLException("해당 이름의 강의가 없습니다.");
        }

        scoreDAO.addScore(member, lecture);
    }

    // 성적 삭제
    public void deleteScore(String memberName, String lectureName) throws SQLException {
        if (memberName == null || memberName.trim().isEmpty() ||
                lectureName == null || lectureName.trim().isEmpty()){
            throw new SQLException("이름과 과목을 제대로 입력해주세요!");
        }

        Members member = membersDAO.searchMembersByMemberId(memberName);
        if (member == null) {
            throw new SQLException("해당 아이디의 학생이 없습니다.");
        }
        Lectures lecture = lecturesDAO.getLectureByFullname(lectureName);
        if (lecture == null) {
            throw new SQLException("해당 이름의 강의가 없습니다.");
        }

        scoreDAO.deleteScore(member, lecture);
    }
}
