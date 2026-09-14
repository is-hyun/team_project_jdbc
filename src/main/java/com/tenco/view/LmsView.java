package com.tenco.view;

import com.tenco.dao.LecturesDAO;
import com.tenco.dao.MembersDAO;
import com.tenco.dao.ScoreDAO;
import com.tenco.dto.Lectures;
import com.tenco.dto.Members;
import com.tenco.service.MemberService;
import com.tenco.dto.Scores;
import com.tenco.dto.SearchAllMembersDTO;
import com.tenco.dto.SearchMembersByIdDTO;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class LmsView {

    private final LecturesDAO lecturesDAO = new LecturesDAO();
    private final MembersDAO membersDAO = new MembersDAO();
    private final ScoreDAO scoreDAO = new ScoreDAO();
    private final Scanner scanner = new Scanner(System.in);
    private final MemberService memberService = new MemberService();
    private Members loggedInMember;

    // =========================================================
    // 프로그램 시작점
    // =========================================================
    public void start() {
        System.out.println("=== LMS 시스템 ===");

        while (true) {
            if (loggedInMember == null) {
                if (!login()) {
                    return;
                }
                continue;
            }
            printMenu();
            int choice = readInt("선택: ");

            try {
                if (!loggedInMember.isAdmin() && (choice == 3 || choice == 5 || choice == 7)) {
                    System.out.println("관리자만 사용할 수 있는 메뉴입니다.");
                    continue;
                }
                switch (choice) {
                    case 1 -> listLectures();   // 강의 목록 조회
                    case 2 -> searchLectures(); // 강의 검색
                    case 3 -> listStudents();
                    case 4 -> searchMember();
                    case 5 -> listAllScores();
                    case 6 -> searchMemberScores();
                    case 7 -> updateScore();
                    case 8 -> {
                        System.out.println("프로그램을 종료합니다.");
                        return;
                    }
                    case 9 -> {
                        loggedInMember = null;
                        System.out.println("로그아웃되었습니다.");
                    }
                    default -> System.out.println("메뉴에 표시된 번호를 입력하세요.");
                }
            } catch (RuntimeException e) {
                System.out.println("오류: " + e.getMessage());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private boolean login() {
        System.out.println("\n=== 로그인 (종료: 0) ===");
        System.out.print("아이디: ");
        if (!scanner.hasNextLine()) {
            return false;
        }
        String memberId = scanner.nextLine().trim();
        if ("0".equals(memberId)) {
            return false;
        }
        System.out.print("비밀번호: ");
        if (!scanner.hasNextLine()) {
            return false;
        }
        String password = scanner.nextLine();
        try {
            loggedInMember = memberService.login(memberId, password);
            if (loggedInMember == null) {
                System.out.println("아이디 또는 비밀번호가 일치하지 않습니다.");
            } else {
                System.out.printf("%s님, %s로 로그인했습니다.%n", loggedInMember.getName(),
                        loggedInMember.isAdmin() ? "관리자" : "학생");
            }
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
        return true;
    }

    // =========================================================
    // 메인 메뉴 출력
    // =========================================================
    private void printMenu() {
        System.out.println("\n=== 학사 관리 시스템 ===");
        System.out.println("1. 강의 목록 조회");
        System.out.println("2. 강의 검색");
        if (loggedInMember.isAdmin()) {
            System.out.println("3. 학생 목록 조회 (관리자메뉴)");
            System.out.println("4. 학생 정보 조회 (관리자메뉴)");
            System.out.println("5. 전체 성적 조회 (관리자메뉴)");
            System.out.println("6. 학생 성적 조회 (관리자메뉴)");
            System.out.println("7. 성적 수정 (관리자메뉴)");
        } else {
            System.out.println("4. 내 정보 조회");
            System.out.println("6. 내 성적 조회");
        }
        System.out.println("8. 종료");
        System.out.println("9. 로그아웃");
    }

    // =========================================================
    // 강의 목록 조회
    // =========================================================
    private void listLectures() {
        List<Lectures> lectures = lecturesDAO.getAllLectures();
        System.out.println("\n=== 강의 목록 ===");

        if (lectures.isEmpty()) {
            System.out.println("등록된 강의가 없습니다.");
            return;
        }

        for (Lectures lecture : lectures) {
            System.out.printf("강의코드: %s | 강의명: %s | 교수: %s | 학점: %d | 정원: %d | 상태: %s%n",
                    lecture.getLectureCode(),
                    lecture.getLectureName(),
                    lecture.getProfessor(),
                    lecture.getCredit(),
                    lecture.getCapacity(),
                    lecture.isAvailable() ? "개설중" : "폐강");
        }
    }

    // =========================================================
    // 강의 검색
    // LecturesDAO.searchLectures(keyword) 를 사용해 키워드로 조회한다.
    // =========================================================
    private void searchLectures() {
        System.out.print("강의명, 강의코드 또는 교수명 입력: ");
        String keyword = scanner.nextLine().trim();
        if (keyword.isEmpty()) {
            System.out.println("검색어를 입력해주세요.");
            return;
        }

        List<Lectures> lectures = lecturesDAO.searchLectures(keyword);
        System.out.println("\n=== 강의 검색 결과 ===");
        if (lectures.isEmpty()) {
            System.out.println("검색 결과가 없습니다.");
            return;
        }

        for (Lectures lecture : lectures) {
            System.out.printf("강의코드: %s | 강의명: %s | 교수: %s | 학점: %d | 정원: %d%n",
                    lecture.getLectureCode(),
                    lecture.getLectureName(),
                    lecture.getProfessor(),
                    lecture.getCredit(),
                    lecture.getCapacity());
        }
    }

    // =========================================================
    // 학생 목록 조회
    // =========================================================
    private void listStudents() {
        List<SearchAllMembersDTO> students = membersDAO.searchAllMembers();
        System.out.println("\n=== 학생 목록 ===");

        if (students.isEmpty()) {
            System.out.println("등록된 학생이 없습니다.");
            return;
        }

        for (SearchAllMembersDTO student : students) {
            System.out.printf("이름: %s | 전화: %s | 전공: %s | 학년: %d%n",
                    student.getName(),
                    student.getPhone(),
                    student.getMajor(),
                    student.getGrade());
        }
    }

    // =========================================================
    // 특정 학생 정보 조회
    // MembersDAO.searchMembersById(int id) 를 이용한다.
    // =========================================================
    private void searchMember() {
        Integer id = loggedInMember.isAdmin() ? findStudentIdByName() : Integer.valueOf(loggedInMember.getId());
        if (id == null) {
            return;
        }
        SearchMembersByIdDTO member = membersDAO.searchMembersById(id);

        if (member == null) {
            System.out.println("해당 학생이 없습니다.");
            return;
        }

        System.out.println("\n=== 학생 정보 ===");
        System.out.printf("이름: %s | 전화: %s | 전공: %s | 학년: %d%n",
                member.getName(),
                member.getPhone(),
                member.getMajor(),
                member.getGrade());
    }

    // =========================================================
    // 전체 성적 조회
    // =========================================================
    private void listAllScores() {
        List<Scores> scores = scoreDAO.getAllScores();
        System.out.println("\n=== 전체 성적 목록 ===");

        if (scores.isEmpty()) {
            System.out.println("등록된 성적이 없습니다.");
            return;
        }

        for (Scores score : scores) {
            System.out.printf("학생: %s | 과목: %s | 점수: %d%n",
                    score.getName(),
                    score.getLectureName(),
                    score.getScore());
        }
    }

    // =========================================================
    // 학생 성적 조회
    // ScoreDAO.getScoresById(String id) 를 사용한다.
    // =========================================================
    private void searchMemberScores() {
        Integer memberId = loggedInMember.isAdmin() ? findStudentIdByName() : Integer.valueOf(loggedInMember.getId());
        if (memberId == null) {
            return;
        }
        List<Scores> scores = scoreDAO.getScoresById(String.valueOf(memberId));

        System.out.println("\n=== 학생 성적 ===");
        if (scores.isEmpty()) {
            System.out.println("해당 학생의 성적이 없습니다.");
            return;
        }

        for (Scores score : scores) {
            System.out.printf("학생: %s | 과목: %s | 점수: %d%n",
                    score.getName(),
                    score.getLectureName(),
                    score.getScore());
        }
    }

    // =========================================================
    // 성적 수정
    // ScoreDAO.updateScore(int memberId, int lectureId, int score) 를 사용한다.
    // =========================================================
    private void updateScore() throws SQLException {
        Integer memberId = findStudentIdByName();
        if (memberId == null) {
            return;
        }
        Integer lectureId = findLectureIdByName();
        if (lectureId == null) {
            return;
        }
        int score = readInt("수정 점수(0~100): ");

        if (score < 0 || score > 100) {
            System.out.println("점수는 0~100 사이여야 합니다.");
            return;
        }

        scoreDAO.updateScore(memberId, lectureId, score);
        System.out.println("성적이 수정되었습니다.");
    }

    // 이름으로 찾은 내부 ID를 기존 DAO에 전달한다. 같은 이름이면 첫 번째 결과를 사용한다.
    private Integer findStudentIdByName() {
        System.out.print("학생 이름: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("학생 이름을 입력해주세요.");
            return null;
        }
        for (SearchAllMembersDTO student : membersDAO.searchAllMembers()) {
            if (name.equals(student.getName())) {
                return student.getId();
            }
        }
        System.out.println("해당 이름의 학생이 없습니다.");
        return null;
    }

    // 강의명으로 강의에 대한 정보를 검색한다.
    private Integer findLectureIdByName() {
        System.out.print("강의명: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("강의명을 입력해주세요.");
            return null;
        }
        for (Lectures lecture : lecturesDAO.getAllLectures()) {
            if (name.equals(lecture.getLectureName())) {
                return lecture.getId();
            }
        }
        System.out.println("해당 이름의 강의가 없습니다.");
        return null;
    }

    // =========================================================
    // 숫자 입력 검증용 메서드
    // 잘못된 값이 들어오면 다시 입력받는다.
    // =========================================================
    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("숫자를 입력해주세요.");
            }
        }
    }
}

