package com.tenco.service;

import com.tenco.dao.MembersDAO;
import com.tenco.dto.Members;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

public class MemberService {

    private final MembersDAO membersDAO = new MembersDAO();

    // !!!
    public Members login(String memberId, String password) {
        if (memberId == null || memberId.isBlank() || password == null || password.isEmpty()) {
            throw new IllegalArgumentException("아이디와 비밀번호를 입력하세요.");
        }
        return membersDAO.login(memberId.trim(), password);
    }

    // 본인 정보 조회 - PK
    public Members getSelfInfoById(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("로그인 후 이용 가능합니다.");
        }

        return membersDAO.searchMembersById(id);
    }

    // 본인 정보 조회 - 학번
    public Members getSelfInfoByMemberId(String memberId) {
        if (memberId == null || memberId.trim().isEmpty()) {
            throw new IllegalArgumentException("로그인 후 이용 가능합니다.");
        }

        return membersDAO.searchMembersByMemberId(memberId);
    }

    // 학생 학번으로 조회
    public Members getMembersById(String memberId) {
        if (memberId == null || memberId.trim().isEmpty()) {
            throw new IllegalArgumentException("학생 ID를 입력해주세요.");
        }

        return membersDAO.searchMembersByMemberId(memberId);
    }

    // 회원가입
    public void registerMember(
            String memberId,
            String password,
            String name,
            String phone,
            String major,
            int grade
    ) {
        if (memberId == null || password == null
                || name == null || phone == null
                || major == null || grade <= 0) {

            throw new IllegalArgumentException(
                    "필수 입력 정보가 누락되었습니다."
            );
        }

        if (!isValidPassword(password)) {
            throw new IllegalArgumentException(
                    "비밀번호는 8자이상 특수문자가 포함되어야 합니다."
            );
        }

        String hashed = BCrypt.hashpw(
                password,
                BCrypt.gensalt(10)
        );

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

    // 전체 학생 조회
    public List<Members> getAllMembers() {
        return membersDAO.searchAllMembers();
    }

    // 학생 이름으로 조회
    public List<Members> getMembersByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "학생 이름을 입력해주세요."
            );
        }

        return membersDAO.searchMembersByName(name);
    }

    // 비밀번호 수정
    public boolean updateMemberPassword(
            int targetId,
            String newPassword
    ) {
        if (targetId <= 0) {
            throw new IllegalArgumentException(
                    "로그인 후 이용 가능합니다."
            );
        }

        if (!isValidPassword(newPassword)) {
            throw new IllegalArgumentException(
                    "비밀번호는 8자이상 특수문자가 포함되어야 합니다."
            );
        }

        String hashed = BCrypt.hashpw(
                newPassword,
                BCrypt.gensalt(10)
        );

        return membersDAO.updateMemberPassword(
                targetId,
                hashed
        );
    }

    // 학번 수정
    public boolean updateMemberId(
            int targetId,
            String newMemberId
    ) {
        if (targetId <= 0) {
            throw new IllegalArgumentException(
                    "올바른 학생 ID를 입력해주세요."
            );
        }

        if (newMemberId == null || newMemberId.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "변경할 학번을 입력해주세요."
            );
        }

        return membersDAO.updateMemberId(
                targetId,
                newMemberId
        );
    }

    // 이름 수정
    public boolean updateMemberName(
            int targetId,
            String newName
    ) {
        if (targetId <= 0) {
            throw new IllegalArgumentException(
                    "올바른 학생 ID를 입력해주세요."
            );
        }

        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "변경할 이름을 입력해주세요."
            );
        }

        return membersDAO.updateMemberName(
                targetId,
                newName
        );
    }

    // 전화번호 수정
    public boolean updateMemberPhone(
            int targetId,
            String newPhone
    ) {
        if (targetId <= 0) {
            throw new IllegalArgumentException(
                    "올바른 학생 ID를 입력해주세요."
            );
        }

        if (newPhone == null || newPhone.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "변경할 번호를 입력해주세요."
            );
        }

        if (!newPhone.matches("[0-9]{11}")) {
            throw new IllegalArgumentException(
                    "전화번호는 숫자 11자리로 입력해주세요. (하이픈, 공백 제외)"
            );
        }

        return membersDAO.updateMemberPhone(
                targetId,
                newPhone
        );
    }

    // 학과 수정
    public boolean updateMemberMajor(
            int targetId,
            String newMajor
    ) {
        if (targetId <= 0) {
            throw new IllegalArgumentException(
                    "올바른 학생 ID를 입력해주세요."
            );
        }

        if (newMajor == null || newMajor.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "변경할 학과를 입력해주세요."
            );
        }

        return membersDAO.updateMemberMajor(
                targetId,
                newMajor
        );
    }

    // 학생 삭제
    public boolean deleteMember(int targetId) {
        if (targetId <= 0) {
            throw new IllegalArgumentException(
                    "잘못 입력하거나 없는 학생입니다."
            );
        }

        return membersDAO.deleteMember(targetId);
    }

    // 비밀번호 유효성 검사
    public boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        return password.matches(
                ".*[!@#$%^&*(),.?\":{}|<>].*"
        );
    }
}