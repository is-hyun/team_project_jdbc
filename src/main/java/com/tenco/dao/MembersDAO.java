package com.tenco.dao;

import com.tenco.dto.Members;
import com.tenco.dto.SearchAllMembersDTO;
import com.tenco.dto.SearchMembersByIdDTO;
import com.tenco.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MembersDAO {
    public Members login(String memberId, String password) {
        String sql = "SELECT id, member_id, password, name, admin FROM members WHERE member_id = ?";
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, memberId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next() && memberId.equals(rs.getString("member_id"))
                        && password.equals(rs.getString("password"))) {
                    // 로그인 상태에는 비밀번호를 보관하지 않는다.
                    return Members.builder()
                            .id(rs.getInt("id"))
                            .memberId(rs.getString("member_id"))
                            .name(rs.getString("name"))
                            .admin(rs.getInt("admin") == 1)
                            .build();
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("로그인 중 DB 오류가 발생했습니다. 연결 및 members 테이블을 확인하세요.", e);
        }
        return null;
    }

    //    학적 정보 조회
//    로그인 ID, PW를 출력할 수 없기에 따로 SearchMembersByIdDTO를 만들어 사용. (필요한 내용만 전달하도록)
    public SearchMembersByIdDTO searchMembersById(int id) {
        String sql = """
                select
                	id,
                    name,
                    phone,
                    major,
                    grade
                from members
                where id = ?;
                """;

        try (Connection connection = DatabaseUtil.getConnection()) {
            PreparedStatement psmt = connection.prepareStatement(sql);
            psmt.setInt(1, id);
            ResultSet rs = psmt.executeQuery();

            if (rs.next()) {
                return SearchMembersByIdDTO.builder()
                        .id(rs.getInt("id"))
                        .name(rs.getString("name"))
                        .phone(rs.getString("phone"))
                        .major(rs.getString("major"))
                        .grade(rs.getInt("grade"))
                        .build();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    //    학생 정보 등록 (관리자)
    private void addMembers(Members members) {
        int rows = 0;
        String sql = """
                insert into
                    members(member_id, password, name, phone, major, grade)
                values (?, ?, ?, ?, ?, ?);
                """;

        try (Connection connection = DatabaseUtil.getConnection()) {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, members.getMemberId());
                pstmt.setString(2, members.getPassword());
                pstmt.setString(3, members.getName());
                pstmt.setString(4, members.getPhone());
                pstmt.setString(5, members.getMajor());
                pstmt.setInt(6, members.getGrade());

                rows = pstmt.executeUpdate();
                System.out.println(rows + "행이 추가됨.");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    //    전체 조회 (관리자)
    public List<SearchAllMembersDTO> searchAllMembers() {
        List<SearchAllMembersDTO> membersList = new ArrayList<>();
        String sql = """
                select
                	id,
                    name,
                    phone,
                    major,
                    grade
                from members;
                """;

        try (Connection connection = DatabaseUtil.getConnection()) {
            PreparedStatement psmt = connection.prepareStatement(sql);
            ResultSet rs = psmt.executeQuery();

            while (rs.next()) {
                membersList.add(SearchAllMembersDTO.builder()
                        .id(rs.getInt("id"))
                        .name(rs.getString("name"))
                        .phone(rs.getString("phone"))
                        .major(rs.getString("major"))
                        .grade(rs.getInt("grade"))
                        .build());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return membersList;
    }

    public static void main(String[] args) {
        MembersDAO membersDAO = new MembersDAO();
        Members members = new Members();


//        membersDAO.addMembers(Members.builder()
//                        .memberId("student11")
//                        .password("pass123")
//                        .name("달유메")
//                        .phone("010-1234-1234")
//                        .major("컴퓨터공학과")
//                        .grade(1)
//                .build());

        System.out.println(membersDAO.searchAllMembers());

    }
}
