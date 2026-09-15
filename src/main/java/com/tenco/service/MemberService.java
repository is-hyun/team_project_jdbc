package com.tenco.service;

import com.tenco.dao.MembersDAO;
import com.tenco.dto.*;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.List;

public class MemberService {
//    private final MembersDAO membersDAO;
//
//    public MemberService() {
//        this(new MembersDAO());
//    }

    /*
    학생 view
                       둘 중 필요없는건 삭제 예정
    ===========================================================
    본인 id 조회 (학생 only) - getSelfInfoById
    로그인 된 id만 조회 가능
    필요 매개변수 : id (pk)

    본인 id 조회 (학생 only) - getSelfInfoByMemberId
    로그인 된 id만 조회 가능
    필요 매개변수 : id (pk)
    ===========================================================

    member_id로 조회 (관리자 only) - getMembersById
    로그인 된 id만 조회 가능
    필요 매개변수 : id (pk)

    학생 정보 등록 (관리자 only) - registerMember
    필요 매개변수 : memberId, password, name, phone, major, grade

    모든 학생 조회(관리자 only) - getAllMembers
    필요 매개변수 : 없음

    학생 이름으로 조회(관리자 only) - getMembersByName
    필요 매개변수 : name

    학생 비밀번호 수정 (학생 only) - updateMemberPassword
    필요 매개변수 : id, password(새로 입력한)
     */

    private final MembersDAO membersDAO = new MembersDAO();

    //    학생 조회
    // ===========================================================================================================================
    //    1. id로 조회
    public Members getSelfInfoById(int id) throws SQLException {
        if (id <= 0) {
            throw new SQLException("로그인 후 이용 가능합니다.");
        }

        return membersDAO.searchMembersById(id);
    }

    //    2. memberId로 조회
    public Members getSelfInfoByMemberId(String memberId) throws SQLException {
        if (memberId == null || memberId.trim().isEmpty()) {
            throw new SQLException("로그인 후 이용 가능합니다.");
        }

        return membersDAO.searchMembersByMemberId(memberId);
    }

    // ===========================================================================================================================

    //    학생 조회 (관리자)
    public Members getMembersById(String memberId) throws SQLException {
        if (memberId == null || memberId.trim().isEmpty()) {
            throw new SQLException("학생 ID를 입력해주세요.");
        }

        return membersDAO.searchMembersByMemberId(memberId);
    }

    //    학생 정보 등록 (관리자)
    public void registerMember(String memberId, String password, String name,
                               String phone, String major, int grade) throws SQLException {

        if (memberId == null || password == null
                || name == null || phone == null
                || major == null || grade <= 0) {
            throw new SQLException("필수 입력 정보가 누락되었습니다.");
        }

//        비밀번호 검증
        if (!isValidPassword(password)) {
            throw new SQLException("비밀번호는 8자이상 특수문자가 포함되어야 합니다.");
        }

        String hashed = BCrypt.hashpw(password, BCrypt.gensalt(10));
        Members members = Members.builder()
                .memberId(memberId)
                .password(hashed)
                .name(name)
                .phone(phone)
                .major(major)
                .grade(grade)
                .build();

        membersDAO.addMember(members);
    }

    //    모든 학생 조회 (관리자)
    public List<Members> getAllMembers() {
        return membersDAO.searchAllMembers();
    }

    public List<Members> getMembersByName(String name) throws SQLException {
        if (name == null || name.trim().isEmpty()) {
            throw new SQLException("학생 ID를 입력해주세요.");
        }

        return membersDAO.searchMembersByName(name);
    }

    //    학생 비밀번호 변경 (학생)
//    DB수정이라서 pk를 사용했습니다.
    public boolean updateMemberPassword(int targetId, String newPassword) throws SQLException {

        if (targetId <= 0) {
            throw new SQLException("로그인 후 이용 가능합니다.");
        }

        //        비밀번호 검증
        if (!isValidPassword(newPassword)) {
            throw new SQLException("비밀번호는 8자이상 특수문자가 포함되어야 합니다.");
        }

        String hashed = BCrypt.hashpw(newPassword, BCrypt.gensalt(10));

        return membersDAO.updateMemberPassword(targetId, hashed);
    }

    //    학생 학번 수정(관리자)
    public boolean updateMemberId(int targetId, String newMemberId) throws SQLException {
        if (targetId <= 0) {
            throw new SQLException("로그인 후 이용 가능합니다.");
        }

        if (newMemberId == null || newMemberId.trim().isEmpty()) {
            throw new SQLException("변경할 학번을 입력해주세요.");
        }

        return membersDAO.updateMemberId(targetId, newMemberId);
    }

    //    학생 이름 변경 (관리자)
    public boolean updateMemberName(int targetId, String newName) throws SQLException {
        if (targetId <= 0) {
            throw new SQLException("로그인 후 이용 가능합니다.");
        }

        if (newName == null || newName.trim().isEmpty()) {
            throw new SQLException("변경할 이름을 입력해주세요.");
        }

        return membersDAO.updateMemberName(targetId, newName);
    }

    //    학생 전화번호 변경 (관리자)
    public boolean updateMemberPhone(int targetId, String newPhone) throws SQLException {
        if (targetId <= 0) {
            throw new SQLException("로그인 후 이용 가능합니다.");
        }

        if (newPhone == null || newPhone.trim().isEmpty()) {
            throw new SQLException("변경할 번호를 입력해주세요.");
        }

        return membersDAO.updateMemberPhone(targetId, newPhone);
    }

    //    학생 학과 변경 (관리자)
    public boolean updateMemberMajor(int targetId, String newMajor) throws SQLException {
        if (targetId <= 0) {
            throw new SQLException("로그인 후 이용 가능합니다.");
        }

        if (newMajor == null || newMajor.trim().isEmpty()) {
            throw new SQLException("변경할 번호를 입력해주세요.");
        }

        return membersDAO.updateMemberMajor(targetId, newMajor);
    }


    //    학생 삭제
    public boolean deleteMember(int targetId) throws SQLException {
        if (targetId <= 0) {
            throw new SQLException("잘못 입력하거나 없는 학생입니다.");
        }

        return membersDAO.deleteMember(targetId);
    }

    public Members login(String memberId, String password) {
        if (memberId == null || memberId.isBlank() || password == null || password.isEmpty()) {
            throw new IllegalArgumentException("아이디와 비밀번호를 입력하세요.");
        }
        return membersDAO.login(memberId.trim(), password);
    }

    //    비밀번호 검증(8자이상 + 특수문자 포함)
    public boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        // 특수문자 최소 1개 포함
        return password.matches(".*[!@#$%^&*(),.?\":{}|<>].*");
    }
}
