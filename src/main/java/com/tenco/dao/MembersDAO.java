package com.tenco.dao;

import com.tenco.dto.*;
import com.tenco.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MembersDAO {
    //    학적 정보 조회
//    id(pk)로 조회
    public SearchMembersByIdDTO searchMembersById(int id) {
        String sql = """
                select
                	m.member_id,
                    m.name,
                    m.phone,
                    m.major,
                    m.grade,
                    s.score
                from members m
                left join scores s
                on m.id = s.member_id
                where m.id = ?;
                """;

        try (Connection connection = DatabaseUtil.getConnection()) {
            PreparedStatement psmt = connection.prepareStatement(sql);
            psmt.setInt(1, id);
            ResultSet rs = psmt.executeQuery();

            if (rs.next()) {
                return SearchMembersByIdDTO.builder()
                        .memberId(rs.getString("member_id"))
                        .name(rs.getString("name"))
                        .phone(rs.getString("phone"))
                        .major(rs.getString("major"))
                        .grade(rs.getInt("grade"))
                        .score(rs.getInt("score"))
                        .build();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

//    memberId 로 조회
    public SearchMembersByMemberIdDTO searchMembersByMemberId(String memberId) {
        String sql = """
                select
                	m.member_id,
                    m.name,
                    m.phone,
                    m.major,
                    m.grade,
                    s.score
                from members m
                left join scores s
                on m.id = s.member_id
                where m.member_id = ?;
                """;

        try (Connection connection = DatabaseUtil.getConnection()) {
            PreparedStatement psmt = connection.prepareStatement(sql);
            psmt.setString(1, memberId);
            ResultSet rs = psmt.executeQuery();

            if (rs.next()) {
                return SearchMembersByMemberIdDTO.builder()
                        .memberId(rs.getString("member_id"))
                        .name(rs.getString("name"))
                        .phone(rs.getString("phone"))
                        .major(rs.getString("major"))
                        .grade(rs.getInt("grade"))
                        .score(rs.getInt("score"))
                        .build();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    //    학생 정보 등록 (관리자)
    public void addMember(Members members) {
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
                    m.member_id,
                    m.name,
                    m.phone,
                    m.major,
                    m.grade,
                    s.score
                from members m
                left join scores s
                on m.id = s.member_id;
                """;

        try (Connection connection = DatabaseUtil.getConnection()) {
            PreparedStatement psmt = connection.prepareStatement(sql);
            ResultSet rs = psmt.executeQuery();

            while (rs.next()) {
                membersList.add(SearchAllMembersDTO.builder()
                        .memberId(rs.getString("member_id"))
                        .name(rs.getString("name"))
                        .phone(rs.getString("phone"))
                        .major(rs.getString("major"))
                        .grade(rs.getInt("grade"))
                        .score(rs.getInt("score"))
                        .build());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return membersList;
    }

    public List<SearchMembersByNameDTO> searchMembersByName(String name) {
        List<SearchMembersByNameDTO> nameList = new ArrayList<>();
        String sql = """
                select
                	m.member_id,
                    m.name,
                    m.phone,
                    m.major,
                    m.grade,
                    s.score
                from members m
                left join scores s
                on m.id = s.member_id
                where m.name = ?;
                """;

        try (Connection connection = DatabaseUtil.getConnection()) {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();

            while(rs.next()) {
                nameList.add(SearchMembersByNameDTO.builder()
                        .memberId(rs.getString("member_id"))
                        .name(rs.getString("name"))
                        .phone(rs.getString("phone"))
                        .major(rs.getString("major"))
                        .grade(rs.getInt("grade"))
                        .score(rs.getInt("score"))
                        .build()
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return nameList;
    }

}
