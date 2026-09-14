package com.tenco.service;

import com.tenco.dao.MembersDAO;
import com.tenco.dto.Members;

public class MemberService {
    private final MembersDAO membersDAO;

    public MemberService() {
        this(new MembersDAO());
    }

    public MemberService(MembersDAO membersDAO) {
        this.membersDAO = membersDAO;
    }

    public Members login(String memberId, String password) {
        if (memberId == null || memberId.isBlank() || password == null || password.isEmpty()) {
            throw new IllegalArgumentException("아이디와 비밀번호를 입력하세요.");
        }
        return membersDAO.login(memberId.trim(), password);
    }
}
