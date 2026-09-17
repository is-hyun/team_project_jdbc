package com.tenco.service;

import com.tenco.dao.MembersDAO;
import com.tenco.dao.RegistrationDAO;
import com.tenco.dto.Members;
import com.tenco.dto.Registration;
import lombok.Data;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

@Data
public class RegistrationService {

    private final RegistrationDAO registrationDAO;
    private final MembersDAO memberDAO;
    // 신청
    public void applyLecture(String memId, String lecId) {
        try {
            registrationDAO.registerLecture(memId, lecId);
            System.out.println("수강 신청이 성공적으로 완료되었습니다.");
        } catch (SQLException e) {
            System.out.println("수강 신청 실패: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // 내 수강신청 조회
    public List<Registration> getMyLectureList(String studentId) {
        Members member = memberDAO.searchMembersByMemberId(studentId);
        if (member == null) {
            System.out.println("존재하지 않는 회원 정보입니다. (학번: " + studentId + ")");
            return null;
        }
        return registrationDAO.getMyRegistrations(studentId);
    }

    // 수강 신청 전체조회
    public List<Registration> getAllLectureList(Members loginUser) throws SQLException {
        if (loginUser == null || !loginUser.isAdmin()) {
            System.out.println("권한이 없습니다. 관리자만 전체 조회가 가능합니다.");
            return null;
        }
        return registrationDAO.getAllRegistrations(loginUser);
    }

    public List<Registration> deleteRegistration(String memId, String lecId) {
        try {
            List<Registration> remainingList = registrationDAO.deleteRegistration(memId, lecId);
            System.out.println("성공적으로 수강 취소되었습니다.");
            return remainingList;

        } catch (SQLException e) {
            System.out.println("수강 취소 실패");
            throw new RuntimeException(e);
        }
    }
}
