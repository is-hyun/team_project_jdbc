package com.tenco.util;

import org.mindrot.jbcrypt.BCrypt;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class HashTest {

    // 모든 회원의 비밀번호를 검사하여 평문인 경우 해시로 일괄 변경
    public static void migratePasswordsToHash() {
        String selectSql = "SELECT id, password FROM members WHERE id <= 12";
        String updateSql = "UPDATE members SET password = ? WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement selectPstmt = conn.prepareStatement(selectSql);
             ResultSet rs = selectPstmt.executeQuery()) {

            int count = 0;
            while (rs.next()) {
                int id = rs.getInt("id");
                String rawPassword = rs.getString("password");

                // 이미 BCrypt 해시된 형태인지 간단히 체크
                if (rawPassword != null && !rawPassword.startsWith("$2a$")) {
                    // BCrypt로 해시 암호화 생성
                    String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt(10));

                    // DB에 해시값으로 업데이트
                    try (PreparedStatement updatePstmt = conn.prepareStatement(updateSql)) {
                        updatePstmt.setString(1, hashedPassword);
                        updatePstmt.setInt(2, id);
                        updatePstmt.executeUpdate();
                        count++;
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("비밀번호 암호화 중 오류 발생: " + e.getMessage(), e);
        }
    }

    // 단독 실행용 메인 (최초 1회 실행 후 주석 처리 또는 삭제)
    public static void main(String[] args) {
        System.out.println("====== 비밀번호 해시 암호화 시작 ======");
        migratePasswordsToHash();
        System.out.println("====== 암호화 완료 ======");
    }
}
