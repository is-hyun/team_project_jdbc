package com.tenco.service;

import com.tenco.dao.LecturesDAO;
import com.tenco.dao.MembersDAO;
import com.tenco.dao.ScoreDAO;
import com.tenco.dto.Lectures;
import com.tenco.dto.Registration;

import java.sql.SQLException;
import java.util.List;

public class Service {

    private final LecturesDAO lecturesDAO = new LecturesDAO();
    private final MembersDAO membersDAO = new MembersDAO();
    private final ScoreDAO scoreDAO = new ScoreDAO();
//    private final RegistrationDAO registrationDAO = new RegistrationDAO();

    // 1. 강의 목록 전체 조회
    public List<Lectures> getAllLectures() throws SQLException {
        return lecturesDAO.getAllLectures();
    }

    // 2. 강의 목록 검색
    public List<Lectures> searchLectures(String keyword) throws SQLException {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new SQLException("검색어를 입력해 주세요");
        }
        return lecturesDAO.searchLectures(keyword);
    }

    // 3. 강의 정보 등록 (관리자)
    public void addLectures(Lectures lectures) throws SQLException {
        if (lectures.getLectureCode() == null || lectures.getLectureCode().trim().isEmpty() ||
        lectures.getLectureName() == null || lectures.getLectureName().trim().isEmpty()) {
            throw new SQLException("강의코드와 강의명은 필수 입력 사항입니다");
        }
        lecturesDAO.addLectures(lectures);
    }

}
