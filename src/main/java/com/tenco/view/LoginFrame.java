package com.tenco.view;

import com.tenco.dto.Members;
import com.tenco.service.MemberService;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private JTextField idField;
    private JPasswordField pwField;
    private JButton loginBtn;

    private MemberService memberService = new MemberService();

    public LoginFrame() {
        setTitle("학사 관리 시스템 - 로그인");
        setSize(350, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        initLayout();
        initEvent();
    }

    private void initLayout() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 아이디 라벨 & 입력창
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("아이디:"), gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        idField = new JTextField(15);
        panel.add(idField, gbc);

        // 비밀번호 라벨 & 입력창
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("비밀번호:"), gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        pwField = new JPasswordField(15);
        panel.add(pwField, gbc);

        // 로그인 버튼
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        loginBtn = new JButton("로그인");
        panel.add(loginBtn, gbc);

        add(panel);
    }

    private void initEvent() {
        loginBtn.addActionListener(e -> attemptLogin());
        pwField.addActionListener(e -> attemptLogin()); // 엔터키 입력 시 로그인
    }

    private void attemptLogin() {
        String memberId = idField.getText().trim();
        String password = new String(pwField.getPassword());

        try {
            Members loginUser = memberService.login(memberId, password);
            if (loginUser != null) {
                JOptionPane.showMessageDialog(this, loginUser.getName() + "님 환영합니다!");

                // 메인 화면으로 이동
                new MainFrame(loginUser);
                this.dispose(); // 로그인 창 닫기
            } else {
                JOptionPane.showMessageDialog(this, "아이디 또는 비밀번호가 일치하지 않습니다.", "로그인 실패", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}
