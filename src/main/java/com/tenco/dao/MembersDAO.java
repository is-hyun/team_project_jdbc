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
