package com.tenco.dao;

import com.tenco.dto.Lectures;
import com.tenco.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LecturesDAO {
    // 강의 목록 전체 조회
    public List<Lectures> getAllLectures() {
        List<Lectures> lecturesList = new ArrayList<>();
        String llsql = """
                SELECT * FROM lectures
                ORDER BY id
                """;

        try (Connection connect = DatabaseUtil.getConnection()) {
            try (PreparedStatement pstmt = connect.prepareStatement(llsql)) {
                try (ResultSet llrs = pstmt.executeQuery()) {
                    while (llrs.next()) {
                        lecturesList.add(createLectures(llrs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return lecturesList;
    }

    // 강의 검색 (강의코드, *강의명, 교수)
    public List<Lectures> searchLectures(String keyword) {
        List<Lectures> lecturesList = new ArrayList<>();
        // 1. 기본 sql 구조
        String searchsql = """
                SELECT * FROM lectures
                WHERE 1 = 1
                """;
        // 2. 검색 조건별 분기 (공란 / 카테고리별)
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        if (hasKeyword) {
            searchsql += """
                    AND (lecture_code LIKE ? OR lecture_name LIKE ? OR professor LIKE ?)
                    """;
        }
        // TODO - 검색어 없는 경우 / 공백 >> SERVICE에서 처리

        // 3. 실행
        try (Connection connect = DatabaseUtil.getConnection()) {
            try (PreparedStatement pstmt = connect.prepareStatement(searchsql)) {
                if (hasKeyword) {
                    pstmt.setString(1, "%" + keyword + "%");
                    pstmt.setString(2, "%" + keyword + "%");
                    pstmt.setString(3, "%" + keyword + "%");
                }
                try (ResultSet srs = pstmt.executeQuery()) {
                    while (srs.next()) {
                        lecturesList.add(createLectures(srs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if (lecturesList.isEmpty()) {
            System.out.println("검색 결과가 없습니다");
        }

        return lecturesList;
    }

    // 강의 정보 신규 등록
    public int addLectures(Lectures lectures) {
        int rows = 0;
        String addsql = """
                INSERT INTO lectures(lecture_code, lecture_name, professor, credit, capacity, avilable)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection connect = DatabaseUtil.getConnection()) {
            try (PreparedStatement pstmt = connect.prepareStatement(addsql)) {
                pstmt.setString(1, lectures.getLectureCode());
                pstmt.setString(2, lectures.getLectureName());
                if (lectures.getProfessor() == null || lectures.getProfessor().trim().isEmpty()) {
                    // default : 미정
                    pstmt.setString(3, "미정");
                } else {
                    pstmt.setString(3, lectures.getProfessor());
                }
                pstmt.setInt(4, lectures.getCredit());
                pstmt.setInt(5, lectures.getCapacity());
                pstmt.setBoolean(6, lectures.isAvailable());
                System.out.println("신규 강의 정보가 " + rows + " 건 추가되었습니다.");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return rows;
    }

    // 메서드 추출
    private static Lectures createLectures(ResultSet rs) throws SQLException {
        Lectures lectures = new Lectures();
        lectures.setId(rs.getInt("id"));
        lectures.setLectureCode(rs.getString("lecture_code"));
        lectures.setLectureName(rs.getString("lecture_name"));
        lectures.setProfessor(rs.getString("professor"));
        lectures.setCredit(rs.getInt("credit"));
        lectures.setCapacity(rs.getInt("capacity"));
        lectures.setAvailable(rs.getBoolean("available"));
        return lectures;
    }

    // TODO - 테스트 삭제 필수!!!
//    public static void main(String[] args) {
//        LecturesDAO dao = new LecturesDAO();

        // 전체 조회 테스트
//        try {
//            List<Lectures> lectures = dao.getAllLectures();
//
//            System.out.println("=== 전체 강의 목록 조회 결과 (총 " + lectures.size() + "건) ===");
//            for (int i = 0; i < lectures.size(); i++) {
//                System.out.println(lectures.get(i).toString());
//            }
//        } catch (Exception e) {
//            System.err.println("테스트 중 오류 발생:");
//            e.printStackTrace();
//        }

        // 검색 기능 테스트
//        System.out.println("========");
//        String keyword1 = "";
//        List<Lectures> result1 = dao.searchLectures(keyword1);
//        System.out.println("검색된 강의 수: " + result1.size() + "건\n");
//        for (int i = 0; i < result1.size(); i++) {
//            System.out.println(result1.get(i).toString());
//        }
//    }
}
