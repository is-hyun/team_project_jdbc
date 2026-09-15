package com.tenco.view;

import com.tenco.dao.MembersDAO;
import com.tenco.dao.RegistrationDAO;
import com.tenco.dao.ScoreDAO;
import com.tenco.dto.Lectures;
import com.tenco.dto.Members;
import com.tenco.dto.Registration;
import com.tenco.service.RegistrationService;
import com.tenco.service.MemberService;
import com.tenco.service.LecturesService;
import com.tenco.service.ScoreService;
import com.tenco.dto.Scores;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class LmsView {

    private final LecturesService lecturesService = new LecturesService();
    private final ScoreService scoreService = new ScoreService();
    // 수강신청 서비스에 회원 DAO를 주입한다.
    private final MembersDAO membersDAO = new MembersDAO();
    private final RegistrationService registrationService =
            new RegistrationService(new RegistrationDAO(), membersDAO);
    // ScoreService.updateScore의 학생/강의 조회 구현 전까지 기존 수정 동작을 유지한다.
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
                switch (choice) {
                    case 1 -> lecturesMenu();
                    case 2 -> scoresMenu();
                    case 3 -> membersMenu();
                    case 4 -> registrationsMenu();
                    case 0 -> {
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
        System.out.println("1. 강의 관련 메뉴");
        System.out.println("2. 성적 관련 메뉴");
        System.out.println("3. 회원 관련 메뉴");
        System.out.println("4. 수강신청 관련 메뉴");
        System.out.println("9. 로그아웃");
        System.out.println("0. 종료");
    }

    private void registrationsMenu() {
        while (true) {
            System.out.println("\n=== 수강신청 관련 메뉴 ===");
            System.out.println("1. 강의 목록 조회");
            System.out.println("2. 수강신청");
            System.out.println("3. 내 수강신청 조회");
            if (loggedInMember.isAdmin()) {
                System.out.println("4. 전체 수강신청 조회");
            }
            System.out.println("0. 이전 메뉴");
            int choice = readInt("선택: ");
            if (choice == 0) return;
            if (choice == 4 && !requireAdmin()) continue;
            try {
                switch (choice) {
                    case 1 -> listLectures();
                    case 2 -> applyLecture();
                    case 3 -> printRegistrations(
                            registrationService.getMyLectureList(loggedInMember.getId()));
                    case 4 -> printRegistrations(
                            registrationService.getAllLectureList(loggedInMember));
                    default -> System.out.println("메뉴에 표시된 번호를 입력하세요.");
                }
            } catch (RuntimeException | SQLException e) {
                System.out.println("오류: " + e.getMessage());
            }
        }
    }

    private void applyLecture() throws SQLException {
        String code = readText("신청할 강의코드: ");
        if (code.isEmpty()) {
            System.out.println("강의코드를 입력해주세요.");
            return;
        }
        for (Lectures lecture : lecturesService.searchLectures(code)) {
            if (code.equals(lecture.getLectureCode())) {
                registrationService.applyLecture(
                        String.valueOf(loggedInMember.getId()), String.valueOf(lecture.getId()));
                return;
            }
        }
        System.out.println("해당 강의가 없습니다.");
    }

    private void printRegistrations(List<Registration> registrations) {
        System.out.println("\n=== 수강신청 내역 ===");
        if (registrations.isEmpty()) {
            System.out.println("수강신청 내역이 없습니다.");
            return;
        }
        for (Registration registration : registrations) {
            System.out.printf("신청번호: %d | 회원번호: %d | 학생: %s | 강의번호: %d%n",
                    registration.getId(), registration.getMemberId(),
                    registration.getMemberName(), registration.getLectureId());
        }
    }

    private void lecturesMenu() {
        while (true) {
            System.out.println("\n=== 강의 관련 메뉴 ===");
            System.out.println("1. 강의 목록 조회");
            System.out.println("2. 강의 검색");
            if (loggedInMember.isAdmin()) {
                System.out.println("3. 강의 등록");
                System.out.println("4. 강의 정보 수정");
                System.out.println("5. 강의 삭제");
            }
            System.out.println("0. 이전 메뉴");
            int choice = readInt("선택: ");
            if (choice == 0) return;
            if (choice >= 3 && choice <= 5 && !requireAdmin()) continue;
            try {
                switch (choice) {
                    case 1 -> listLectures();
                    case 2 -> searchLectures();
                    case 3 -> addLecture();
                    case 4 -> updateLecture();
                    case 5 -> deleteLecture();
                    default -> System.out.println("메뉴에 표시된 번호를 입력하세요.");
                }
            } catch (RuntimeException | SQLException e) {
                System.out.println("오류: " + e.getMessage());
            }
        }
    }

    private void scoresMenu() {
        while (true) {
            System.out.println("\n=== 성적 관련 메뉴 ===");
            System.out.println(loggedInMember.isAdmin() ? "1. 학생 성적 조회" : "1. 내 성적 조회");
            if (loggedInMember.isAdmin()) {
                System.out.println("2. 전체 성적 조회");
                System.out.println("3. 성적 수정");
            }
            System.out.println("0. 이전 메뉴");
            int choice = readInt("선택: ");
            if (choice == 0) return;
            if (choice >= 2 && choice <= 5 && !requireAdmin()) continue;
            try {
                switch (choice) {
                    case 1 -> searchMemberScores();
                    case 2 -> listAllScores();
                    case 3 -> updateScore();
                    // 서비스의 대상 조회 및 DAO의 SQL 실행 구현이 완료되면 연결한다.
                    case 4, 5 -> System.out.println("아직 사용할 수 없는 기능입니다.");
                    default -> System.out.println("메뉴에 표시된 번호를 입력하세요.");
                }
            } catch (RuntimeException | SQLException e) {
                System.out.println("오류: " + e.getMessage());
            }
        }
    }

    private void membersMenu() {
        while (true) {
            System.out.println("\n=== 회원 관련 메뉴 ===");
            System.out.println(loggedInMember.isAdmin() ? "1. 학생 아이디로 조회" : "1. 내 정보 조회");
            if (loggedInMember.isAdmin()) {
                System.out.println("2. 학생 목록 조회");
                System.out.println("3. 학생 이름으로 조회");
                System.out.println("4. 학생 등록");
            }
            System.out.println("0. 이전 메뉴");
            int choice = readInt("선택: ");
            if (choice == 0) return;
            if (choice >= 2 && choice <= 4 && !requireAdmin()) continue;
            try {
                switch (choice) {
                    case 1 -> searchMember();
                    case 2 -> listStudents();
                    case 3 -> searchMembersByName();
                    case 4 -> registerMember();
                    default -> System.out.println("메뉴에 표시된 번호를 입력하세요.");
                }
            } catch (RuntimeException | SQLException e) {
                System.out.println("오류: " + e.getMessage());
            }
        }
    }

    private boolean requireAdmin() {
        if (loggedInMember.isAdmin()) return true;
        System.out.println("관리자만 사용할 수 있는 메뉴입니다.");
        return false;
    }

    private String readText(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private int readPositiveInt(String prompt) {
        while (true) {
            int value = readInt(prompt);
            if (value > 0) return value;
            System.out.println("1 이상의 숫자를 입력해주세요.");
        }
    }

    private int readOptionalPositiveInt(String prompt) {
        while (true) {
            String input = readText(prompt);
            if (input.isEmpty()) return 0;
            try {
                int value = Integer.parseInt(input);
                if (value > 0) return value;
            } catch (NumberFormatException ignored) {
            }
            System.out.println("1 이상의 숫자를 입력하거나 엔터를 눌러주세요.");
        }
    }

    private boolean readAvailable(boolean current, boolean allowBlank) {
        while (true) {
            String input = readText("상태 (가능 / 불가능" + (allowBlank ? ", 엔터: 유지" : "") + "): ");
            if (allowBlank && input.isEmpty()) return current;
            if ("가능".equals(input)) return true;
            if ("불가능".equals(input)) return false;
            System.out.println("가능 또는 불가능을 입력해주세요.");
        }
    }

    private void addLecture() throws SQLException {
        Lectures lecture = Lectures.builder()
                .lectureCode(readText("강의코드: "))
                .lectureName(readText("강의명: "))
                .professor(readText("교수명: "))
                .credit(readPositiveInt("학점: "))
                .capacity(readPositiveInt("정원: "))
                .available(readAvailable(true, false))
                .build();
        System.out.println(lecturesService.addLectures(lecture)
                ? "강의가 등록되었습니다." : "강의를 등록하지 못했습니다.");
    }

    private void updateLecture() throws SQLException {
        String code = readText("수정할 강의코드: ");
        Lectures selected = null;
        for (Lectures lecture : lecturesService.searchLectures(code)) {
            if (code.equals(lecture.getLectureCode())) {
                selected = lecture;
                break;
            }
        }
        if (selected == null) {
            System.out.println("해당 강의가 없습니다.");
            return;
        }
        System.out.printf("수정 대상: %s (%s)%n", selected.getLectureName(), selected.getLectureCode());
        System.out.println("변경하지 않을 항목은 엔터를 눌러주세요.");
        Lectures changes = Lectures.builder()
                .id(selected.getId())
                .lectureCode(readText("새 강의코드: "))
                .lectureName(readText("새 강의명: "))
                .professor(readText("새 교수명 (삭제/미정 입력 가능): "))
                .credit(readOptionalPositiveInt("새 학점: "))
                .capacity(readOptionalPositiveInt("새 정원: "))
                .available(readAvailable(selected.isAvailable(), true))
                .build();
        System.out.println(lecturesService.updateLectures(changes)
                ? "강의 정보가 수정되었습니다." : "강의 정보를 수정하지 못했습니다.");
    }

    private void deleteLecture() {
        String code = readText("삭제할 강의코드: ");
        if (code.isEmpty()) {
            System.out.println("강의코드를 입력해주세요.");
            return;
        }
        String confirmation = readText("강의 [" + code + "]를 정말 삭제하시겠습니까? (예 / 아니오): ");
        if (!"예".equals(confirmation)) {
            System.out.println("강의 삭제를 취소했습니다.");
            return;
        }
        System.out.println(lecturesService.deleteLectures(code)
                ? "강의가 삭제되었습니다." : "강의를 삭제하지 못했습니다.");
    }

    // =========================================================
    // 강의 목록 조회
    // =========================================================
    private void listLectures() throws SQLException {
        List<Lectures> lectures = lecturesService.getAllLectures();
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
    // 검색어 검증과 조회는 LecturesService에 위임한다.
    // =========================================================
    private void searchLectures() throws SQLException {
        System.out.print("강의명, 강의코드 또는 교수명 입력: ");
        String keyword = scanner.nextLine().trim();
        List<Lectures> lectures = lecturesService.searchLectures(keyword);
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
        List<Members> students = memberService.getAllMembers();
        System.out.println("\n=== 학생 목록 ===");

        if (students.isEmpty()) {
            System.out.println("등록된 학생이 없습니다.");
            return;
        }

        for (Members student : students) {
            printMember(student);
        }
    }

    // =========================================================
    // 특정 학생 정보 조회
    // 학생은 로그인한 본인만 조회하고, 관리자는 입력한 아이디로 조회한다.
    // =========================================================
    private void searchMember() throws SQLException {
        Members member = loggedInMember.isAdmin()
                ? memberService.getMembersById(readText("학생 아이디: "))
                : memberService.getSelfInfoById(loggedInMember.getId());

        if (member == null) {
            System.out.println("해당 학생이 없습니다.");
            return;
        }

        System.out.println("\n=== 학생 정보 ===");
        printMember(member);
    }

    private void printMember(Members member) {
        System.out.printf("아이디: %s | 이름: %s | 전화: %s | 전공: %s | 학년: %d | 평균 점수: %s%n",
                member.getMemberId(), member.getName(), member.getPhone(), member.getMajor(),
                member.getGrade(), member.getScore() == null ? "미등록" : member.getScore());
    }

    private void searchMembersByName() throws SQLException {
        List<Members> students = memberService.getMembersByName(readText("학생 이름: "));
        if (students.isEmpty()) {
            System.out.println("해당 이름의 학생이 없습니다.");
            return;
        }
        students.forEach(this::printMember);
    }

    private void registerMember() throws SQLException {
        String memberId = readRequiredText("학생 아이디: ");
        System.out.print("비밀번호: ");
        String password = scanner.nextLine();
        while (password.isBlank()) {
            System.out.print("비밀번호를 입력해주세요: ");
            password = scanner.nextLine();
        }
        String name = readRequiredText("이름: ");
        String phone = readRequiredText("전화번호: ");
        String major = readRequiredText("전공: ");
        int grade = readPositiveInt("학년: ");
        memberService.registerMember(memberId, password, name, phone, major, grade);
        System.out.println("학생이 등록되었습니다.");
    }

    private String readRequiredText(String prompt) {
        while (true) {
            String value = readText(prompt);
            if (!value.isEmpty()) return value;
            System.out.println("필수 입력 항목입니다.");
        }
    }

    // =========================================================
    // 전체 성적 조회
    // =========================================================
    private void listAllScores() {
        List<Scores> scores = scoreService.getAllScores();
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
    // ScoreService.getScoresById(String id) 를 사용한다.
    // =========================================================
    private void searchMemberScores() {
        Integer memberId = loggedInMember.isAdmin() ? findStudentIdByName() : Integer.valueOf(loggedInMember.getId());
        if (memberId == null) {
            return;
        }
        List<Scores> scores = scoreService.getScoresById(String.valueOf(memberId));

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
    // 선택한 학생과 강의의 ID를 객체에 담아 ScoreDAO에 전달한다.
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

        // 받아온 Integer
        Members member = Members.builder().id(memberId).build();
        Lectures lecture = Lectures.builder().id(lectureId).build();
        scoreDAO.updateScore(member, lecture, score);
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
        for (Members student : memberService.getAllMembers()) {
            if (name.equals(student.getName())) {
                return student.getId();
            }
        }
        System.out.println("해당 이름의 학생이 없습니다.");
        return null;
    }

    // 강의명으로 강의에 대한 정보를 검색한다.
    private Integer findLectureIdByName() throws SQLException {
        System.out.print("강의명: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("강의명을 입력해주세요.");
            return null;
        }
        for (Lectures lecture : lecturesService.searchLectures(name)) {
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

