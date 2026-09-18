package com.tenco.dao;

import com.tenco.dto.Lectures;
import com.tenco.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LecturesDAO {
    // 쿼리
    private static final String BASE_SELECT_SQL = """
            SELECT l.id, l.lecture_code, l.lecture_name, l.professor, l.credit, l.capacity,
                   (SELECT COUNT(*) FROM registration r WHERE r.lecture_id = l.id) AS enrolled
            FROM lectures l
            """;


    // 강의 목록 전체 조회
    public List<Lectures> getAllLectures() {
        List<Lectures> lecturesList = new ArrayList<>();
        String llsql = BASE_SELECT_SQL +
                """
                ORDER BY l.id
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
        String searchsql = BASE_SELECT_SQL +
                """
                WHERE 1 = 1
                """;
        // 2. 검색 조건별 분기 (공란 / 카테고리별)
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        if (hasKeyword) {
            searchsql += """
                    AND (lecture_code LIKE ? OR lecture_name LIKE ? OR professor LIKE ?)
                    """;
        }

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
                INSERT INTO lectures(lecture_code, lecture_name, professor, credit, capacity)
                VALUES (?, ?, ?, ?, ?)
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
                rows = pstmt.executeUpdate();
                System.out.println("신규 강의 정보가 " + rows + " 건 추가되었습니다.");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return rows;
    }

    // 강의 정보 수정
    public int updateLecture(Lectures lectures) {
        int rows = 0;
        String updatesql = """
                UPDATE lectures
                SET lecture_code = ?, lecture_name = ?, professor = ?, credit = ?, capacity = ?
                WHERE id = ?
                """;

        try (Connection connect = DatabaseUtil.getConnection()) {
            try (PreparedStatement pstmt = connect.prepareStatement(updatesql)) {
                pstmt.setString(1, lectures.getLectureCode());
                pstmt.setString(2, lectures.getLectureName());
                pstmt.setString(3, lectures.getProfessor());
                pstmt.setInt(4, lectures.getCredit());
                pstmt.setInt(5, lectures.getCapacity());
                pstmt.setInt(6, lectures.getId());
                rows = pstmt.executeUpdate();
                System.out.println("강의 정보가 수정되었습니다 | 강의ID : " + lectures.getId());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return rows;
    }

    // 강의 삭제 기능 (관리자)
    public int deleteLectures(String code) throws SQLException {
        int rows = 0;
        String deletesql = """
                DELETE FROM lectures
                WHERE lecture_code = ?
                """;

        try (Connection connect = DatabaseUtil.getConnection()) {
            try (PreparedStatement pstmt = connect.prepareStatement(deletesql)) {
                pstmt.setString(1, code);
                rows = pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            // TODO - 추후 토의 후 수정
            throw new SQLException("데이터베이스 제약 조건으로 인해 삭제할 수 없습니다. (수강 중인 학생이 있을 수 있습니다.)");
        }
        return rows;
    }

    // 강의 ID로 단건조회 (내부에서만 사용)
    public Lectures getLectureById(int id) {
        String sql = BASE_SELECT_SQL + "WHERE id = ?";

        try (Connection connect = DatabaseUtil.getConnection()) {
            try (PreparedStatement pstmt = connect.prepareStatement(sql)) {
                pstmt.setInt(1, id);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return createLectures(rs);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    // 강의코드로 단건조회 (내부에서만 사용)
    public Lectures getLectureByCode(String code) {
        String sql = BASE_SELECT_SQL + "WHERE lecture_code = ?";

        try (Connection connect = DatabaseUtil.getConnection()) {
            try (PreparedStatement pstmt = connect.prepareStatement(sql)) {
                pstmt.setString(1, code);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return createLectures(rs);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    // 강의명(전체)로 단건조회 (내부에서만 사용)
    // !!! 완전히 동일한 강의명이 있다면 먼저 저장된 값이 나옴
    public Lectures getLectureByFullname(String name) {
        String sql = BASE_SELECT_SQL + "WHERE lecture_name = ?";

        try (Connection connect = DatabaseUtil.getConnection()) {
            try (PreparedStatement pstmt = connect.prepareStatement(sql)) {
                pstmt.setString(1, name);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return createLectures(rs);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
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
        // lectures.setAvailable(rs.getBoolean("available"));
        lectures.setEnrolled(rs.getInt("enrolled"));
        return lectures;
    }

}
