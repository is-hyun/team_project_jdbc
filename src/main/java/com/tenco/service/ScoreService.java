package com.tenco.service;

import com.tenco.dao.LecturesDAO;
import com.tenco.dao.MembersDAO;
import com.tenco.dao.ScoreDAO;
import com.tenco.dto.Lectures;
import com.tenco.dto.Members;
import com.tenco.dto.Scores;

import java.sql.SQLException;
import java.util.List;

public class Service {

    private LecturesDAO lecturesDAO = new LecturesDAO();
    private MembersDAO membersDAO = new MembersDAO();
    private ScoreDAO scoreDAO = new ScoreDAO();

    ///////////////////////////////////////////////////////
    // Score 관련 기능
    ///////////////////////////////////////////////////////

    // 성적 전체 조회 (관리자 전용)
    public List<Scores> getAllScores(){
        return scoreDAO.getAllScores();
    }

    // 본인 성적 조회 (멤버 로그인 대상 전용)
    // 로그인 대상으로 하는 메서드인데 안전 코드가 필요할까?
    public List<Scores> getScoresById(String memberId){
        return scoreDAO.getScoresById(memberId);
    }

    // 성적 수정
    public void updateScore(String member_name, String lecture_name, Integer score) throws SQLException {
        if (member_name == null || member_name.trim().isEmpty() ||
                lecture_name == null || lecture_name.trim().isEmpty()){
            throw new SQLException("이름과 과목을 입력해주세요!");
        }

        if (score < 0 || score > 100){
            throw new SQLException("성적을 제대로 입력해주세요");
        }



    }

    // 성적 추가
    public void addScore(String memberId, String lectureName){

    }
}
