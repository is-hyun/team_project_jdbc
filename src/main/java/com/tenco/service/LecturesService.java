package com.tenco.service;

import com.tenco.dao.LecturesDAO;
import com.tenco.dao.MembersDAO;
import com.tenco.dao.ScoreDAO;
import com.tenco.dto.Lectures;

import java.sql.SQLException;
import java.util.List;

public class LecturesService {

    private final LecturesDAO lecturesDAO = new LecturesDAO();

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
    public boolean addLectures(Lectures lectures) throws SQLException {
        if (lectures.getLectureCode() == null || lectures.getLectureCode().trim().isEmpty() ||
        lectures.getLectureName() == null || lectures.getLectureName().trim().isEmpty()) {
            throw new SQLException("강의코드와 강의명은 필수 입력 사항입니다");
        }
        int result = lecturesDAO.addLectures(lectures);
        return result > 0;
    }

    // 4. 강의 정보 수정 (관리자)
    public boolean updateLectures(Lectures newLecture) {
        // 기존 데이터 조회
        Lectures exLecture = lecturesDAO.getLectureById(newLecture.getId());
        if (exLecture == null) {
            return false;
        }

        if (newLecture.getLectureCode() != null && !newLecture.getLectureCode().trim().isEmpty()) {
            exLecture.setLectureCode(newLecture.getLectureCode());
        }
        if (newLecture.getLectureName() != null && !newLecture.getLectureName().trim().isEmpty()) {
            exLecture.setLectureName(newLecture.getLectureName());
        }
        // 교수 - view에서 삭제 / 미정으로 입력하면 미정으로 수정
        if (newLecture.getProfessor() != null && !newLecture.getProfessor().trim().isEmpty()) {
            String prof = newLecture.getProfessor().trim();
            if (prof.equals("삭제") || prof.equals("미정")) {
                exLecture.setProfessor("미정");
            } else {
                exLecture.setProfessor(prof);
            }
        }
        if (!(newLecture.getCredit() <= 0)) {
            exLecture.setCredit(newLecture.getCredit());
        }
        if (!(newLecture.getCapacity() <= 0)) {
            exLecture.setCapacity(newLecture.getCapacity());
        }
        exLecture.setAvailable(newLecture.isAvailable());

        int result = lecturesDAO.updateLecture(exLecture);
        return result > 0;
    }

    // 5. 강의 정보 수정 (관리자)
    public boolean deleteLectures(String code) {
        if (code == null || code.trim().isEmpty()) {
            return false;
        }

        String targetCode = code.trim();
        Lectures lecture = lecturesDAO.getLectureByCode(targetCode);
        if (lecture == null) {
            System.out.println("해당 강의 코드와 일치하는 강의가 존재하지 않습니다");
            return false;
        }

        int result = lecturesDAO.deleteLectures(targetCode);
        return result > 0;
    }
}
