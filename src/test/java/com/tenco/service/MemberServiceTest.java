//package com.tenco.service;
//
//import com.tenco.dao.MembersDAO;
//import com.tenco.dto.Members;
//import org.junit.jupiter.api.Test;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class MemberServiceTest {
//    @Test
//    void rejectsMissingCredentialsBeforeDatabaseAccess() {
//        MemberService service = new MemberService(new MembersDAO() {
//            @Override
//            public Members login(String memberId, String password) {
//                fail("빈 입력으로 DB에 접근하면 안 됩니다.");
//                return null;
//            }
//        });
//        assertThrows(IllegalArgumentException.class, () -> service.login(null, "pw"));
//        assertThrows(IllegalArgumentException.class, () -> service.login("  ", "pw"));
//        assertThrows(IllegalArgumentException.class, () -> service.login("student", null));
//        assertThrows(IllegalArgumentException.class, () -> service.login("student", ""));
//    }
//
//    @Test
//    void preservesPasswordWhitespaceAndReturnedIdentity() {
//        Members expected = Members.builder().id(42).memberId("student").admin(false).build();
//        MemberService service = new MemberService(new MembersDAO() {
//            @Override
//            public Members login(String memberId, String password) {
//                assertEquals("student", memberId);
//                assertEquals(" pw ", password);
//                return expected;
//            }
//        });
//        assertSame(expected, service.login(" student ", " pw "));
//    }
//
//    @Test
//    void failedLoginDoesNotCreateStudentSession() {
//        MemberService service = new MemberService(new MembersDAO() {
//            @Override
//            public Members login(String memberId, String password) {
//                return null;
//            }
//        });
//        assertNull(service.login("unknown", "wrong"));
//    }
//}
