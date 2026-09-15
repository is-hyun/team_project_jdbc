package com.tenco.dao;

import com.tenco.dto.*;
import com.tenco.util.DatabaseUtil;
import org.mindrot.jbcrypt.BCrypt;

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
                        && BCrypt.checkpw(password, rs.getString("password"))) {
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
//    id(pk)로 조회
    public Members searchMembersById(int id) {
        String sql = """
                select
                    m.id, m.member_id,
                    m.name,
                    m.phone,
                    m.major,
                    m.grade,
                    round(avg(s.score)) as score
                from members m
                left join scores s
                on m.id = s.member_id
                where m.id = ?
                group by m.id, m.member_id, m.name, m.phone, m.major, m.grade;
                """;

        try (Connection connection = DatabaseUtil.getConnection()) {
            PreparedStatement psmt = connection.prepareStatement(sql);
            psmt.setInt(1, id);
            ResultSet rs = psmt.executeQuery();

            if (rs.next()) {
                return Members.builder()
                        .id(rs.getInt("id"))
                        .memberId(rs.getString("member_id"))
                        .name(rs.getString("name"))
                        .phone(rs.getString("phone"))
                        .major(rs.getString("major"))
                        .grade(rs.getInt("grade"))
                        .score(rs.getObject("score") == null ? null : rs.getInt("score"))
                        .build();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

//    memberId 로 조회
    public Members searchMembersByMemberId(String memberId) {
        String sql = """
                select
                    m.id, m.member_id,
                    m.name,
                    m.phone,
                    m.major,
                    m.grade,
                    round(avg(s.score)) as score
                from members m
                left join scores s
                on m.id = s.member_id
                where m.member_id = ?
                group by m.id, m.member_id, m.name, m.phone, m.major, m.grade;
                """;

        try (Connection connection = DatabaseUtil.getConnection()) {
            PreparedStatement psmt = connection.prepareStatement(sql);
            psmt.setString(1, memberId);
            ResultSet rs = psmt.executeQuery();

            if (rs.next()) {
                return Members.builder()
                        .id(rs.getInt("id"))
                        .memberId(rs.getString("member_id"))
                        .name(rs.getString("name"))
                        .phone(rs.getString("phone"))
                        .major(rs.getString("major"))
                        .grade(rs.getInt("grade"))
                        .score(rs.getObject("score") == null ? null : rs.getInt("score"))
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
    public List<Members> searchAllMembers() {
        List<Members> membersList = new ArrayList<>();
        String sql = """
                select
                    m.id, m.member_id,
                    m.name,
                    m.phone,
                    m.major,
                    m.grade,
                    round(avg(s.score)) as score
                from members m
                left join scores s
                on m.id = s.member_id
                group by m.id, m.member_id, m.name, m.phone, m.major, m.grade;
                """;

        try (Connection connection = DatabaseUtil.getConnection()) {
            PreparedStatement psmt = connection.prepareStatement(sql);
            ResultSet rs = psmt.executeQuery();

            while (rs.next()) {
                membersList.add(Members.builder()
                        .id(rs.getInt("id"))
                        .memberId(rs.getString("member_id"))
                        .name(rs.getString("name"))
                        .phone(rs.getString("phone"))
                        .major(rs.getString("major"))
                        .grade(rs.getInt("grade"))
                        .score(rs.getObject("score") == null ? null : rs.getInt("score"))
                        .build());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return membersList;
    }

    public List<Members> searchMembersByName(String name) {
        List<Members> nameList = new ArrayList<>();
        String sql = """
                select
                    m.id, m.member_id,
                    m.name,
                    m.phone,
                    m.major,
                    m.grade,
                    round(avg(s.score)) as score
                from members m
                left join scores s
                on m.id = s.member_id
                where m.name = ?
                group by m.id, m.member_id, m.name, m.phone, m.major, m.grade;
                """;

        try (Connection connection = DatabaseUtil.getConnection()) {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();

            while(rs.next()) {
                nameList.add(Members.builder()
                        .id(rs.getInt("id"))
                        .memberId(rs.getString("member_id"))
                        .name(rs.getString("name"))
                        .phone(rs.getString("phone"))
                        .major(rs.getString("major"))
                        .grade(rs.getInt("grade"))
                        .score(rs.getObject("score") == null ? null : rs.getInt("score"))
                        .build()
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return nameList;
    }

}
