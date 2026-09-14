package com.tenco;


import com.tenco.dao.RegistrationDAO;
import com.tenco.dto.Registration;

import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception{

        RegistrationDAO registrationDAO = new RegistrationDAO();

        List<Registration> registrations = registrationDAO.getAllRegistrations();


        for (Registration reg : registrations) {
            System.out.println("ID: " + reg.getId() +
                    ", MemberID: " + reg.getMemberId() +
                    ", LectureID: " + reg.getLectureId());

            // 조회된 데이터가 해당 학생의 ID와 일치하는지 검증
        }
    }
}