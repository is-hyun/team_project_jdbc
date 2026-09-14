package com.tenco.service;

import com.tenco.dao.MembersDAO;
import com.tenco.dto.*;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.List;

public class MemberService {
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
     */

    private final MembersDAO membersDAO = new MembersDAO();

    //    학생 조회
    // ===========================================================================================================================
    //    1. id로 조회
    public SearchMembersByIdDTO getSelfInfoById(int id) throws SQLException {
        if (id <= 0) {
            throw new SQLException("로그인 후 이용 가능합니다.");
        }

        return membersDAO.searchMembersById(id);
    }

    //    2. memberId로 조회
    public SearchMembersByMemberIdDTO getSelfInfoByMemberId(String memberId) throws SQLException {
        if (memberId == null || memberId.trim().isEmpty()) {
            throw new SQLException("로그인 후 이용 가능합니다.");
        }

        return membersDAO.searchMembersByMemberId(memberId);
    }

    // ===========================================================================================================================

    //    학생 조회 (관리자)
    public SearchMembersByMemberIdDTO getMembersById(String memberId) throws SQLException {
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
    public List<SearchAllMembersDTO> getAllMembers() {
        return membersDAO.searchAllMembers();
    }

    public List<SearchMembersByNameDTO> getMembersByName(String name) throws SQLException {
        if (name == null || name.trim().isEmpty()) {
            throw new SQLException("학생 ID를 입력해주세요.");
        }

        return membersDAO.searchMembersByName(name);
    }

    public Members login(String memberId, String password) {
        if (memberId == null || memberId.isBlank() || password == null || password.isEmpty()) {
            throw new IllegalArgumentException("아이디와 비밀번호를 입력하세요.");
        }
        return membersDAO.login(memberId.trim(), password);
    }
}
