package com.tenco.view;

import com.tenco.dao.MembersDAO;
import com.tenco.dao.RegistrationDAO;
import com.tenco.dto.Lectures;
import com.tenco.dto.Members;
import com.tenco.dto.Registration;
import com.tenco.dto.Scores;
import com.tenco.service.LecturesService;
import com.tenco.service.MemberService;
import com.tenco.service.RegistrationService;
import com.tenco.service.ScoreService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

public class MainFrame extends JFrame {
    private Members loginUser;

    // Services
    private LecturesService lecturesService = new LecturesService();
    private RegistrationDAO registrationDAO = new RegistrationDAO();
    private MembersDAO membersDAO = new MembersDAO();
    private RegistrationService registrationService = new RegistrationService(registrationDAO, membersDAO);
    private ScoreService scoreService = new ScoreService();
    private MemberService memberService = new MemberService();

    public MainFrame(Members loginUser) {
        this.loginUser = loginUser;

        setTitle("학사 관리 시스템 - " + loginUser.getName() + (loginUser.isAdmin() ? " [관리자]" : " [학생]"));
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 프레임 레이아웃 설정
        setLayout(new BorderLayout());

        // 상단: 사용자 정보 및 로그아웃 버튼 영역
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel infoLabel = new JLabel(loginUser.getName() + " (" + loginUser.getMemberId() + ") 님 접속중");
        JButton logoutBtn = new JButton("로그아웃");

        logoutBtn.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this, "로그아웃 하시겠습니까?", "로그아웃", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                this.dispose();
                new LoginFrame().setVisible(true); // 로그인 화면으로 복귀
            }
        });

        topPanel.add(infoLabel);
        topPanel.add(logoutBtn);
        add(topPanel, BorderLayout.NORTH);

        // 중앙: 권한에 따른 탭Pane 구성
        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("강의 목록 및 수강신청", createLecturePanel());
        // 권한에 따른 탭 분기
        if (!loginUser.isAdmin()) {
            // 🎓 학생 계정일 때
            tabbedPane.addTab("내 수강/성적 조회", createMyInfoPanel());
            tabbedPane.addTab("내 학적 정보", createStudentProfilePanel());
        } else {
            // 👨‍💼 관리자 계정일 때
            tabbedPane.addTab("[관리자] 강의 관리", createAdminLecturePanel());
            tabbedPane.addTab("[관리자] 성적 관리", createAdminScorePanel());
            tabbedPane.addTab("[관리자] 학생 정보 관리", createAdminMemberPanel());
            tabbedPane.addTab("[관리자] 전체 수강신청 현황", createAdminRegistrationPanel()); // 👈 추가된 전체 수강신청 조회 탭
        }

        add(tabbedPane, BorderLayout.CENTER);
        setVisible(true);
    }

    // 1. 강의 목록 및 수강신청 패널
    private JPanel createLecturePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel searchPanel = new JPanel();
        JTextField searchField = new JTextField(15);
        JButton searchBtn = new JButton("검색");
        JButton allBtn = new JButton("전체보기");
        JButton applyBtn = new JButton("수강신청");

        if (loginUser.isAdmin()) {
            applyBtn.setEnabled(false);
            applyBtn.setToolTipText("관리자는 수강신청을 할 수 없습니다.");
        }

        searchPanel.add(new JLabel("검색어:"));
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        searchPanel.add(allBtn);
        searchPanel.add(applyBtn);
        panel.add(searchPanel, BorderLayout.NORTH);

        String[] cols = {"ID", "강의코드", "강의명", "교수", "학점", "정원", "신청가능여부"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(model);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        Runnable loadLectures = () -> {
            model.setRowCount(0);
            try {
                List<Lectures> list = lecturesService.getAllLectures();
                for (Lectures l : list) {
                    model.addRow(new Object[]{
                            l.getId(), l.getLectureCode(), l.getLectureName(),
                            l.getProfessor(), l.getCredit(), l.getCapacity(), l.isAvailable() ? "가능" : "마감"
                    });
                }
            } catch (SQLException e) { e.printStackTrace(); }
        };
        loadLectures.run();

        searchBtn.addActionListener(e -> {
            String kw = searchField.getText().trim();
            try {
                List<Lectures> list = lecturesService.searchLectures(kw);
                model.setRowCount(0);
                for (Lectures l : list) {
                    model.addRow(new Object[]{
                            l.getId(), l.getLectureCode(), l.getLectureName(),
                            l.getProfessor(), l.getCredit(), l.getCapacity(), l.isAvailable() ? "가능" : "마감"
                    });
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        });

        allBtn.addActionListener(e -> loadLectures.run());

        applyBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "신청할 강의를 선택하세요.");
                return;
            }
            int lecId = (int) model.getValueAt(row, 0);

            try {
                // 1. 이미 신청한 강의인지 중복 체크
                List<Registration> myRegs = registrationService.getMyLectureList(loginUser.getMemberId());
                if (myRegs != null) {
                    boolean alreadyApplied = myRegs.stream().anyMatch(r -> r.getLectureId() == lecId);
                    if (alreadyApplied) {
                        JOptionPane.showMessageDialog(this, "이미 수강신청한 강의입니다.");
                        return;
                    }
                }

                // 2. 수강신청 진행
                registrationService.applyLecture(String.valueOf(loginUser.getId()), String.valueOf(lecId));
                JOptionPane.showMessageDialog(this, "수강신청이 완료되었습니다.");
                loadLectures.run();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "수강신청 실패: " + ex.getMessage());
            }
        });

        return panel;
    }

    // 2. 내 수강/성적 조회 패널
    private JPanel createMyInfoPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1));

        // 상단: 내 수강 내역
        JPanel regPanel = new JPanel(new BorderLayout());
        regPanel.setBorder(BorderFactory.createTitledBorder("내 수강 신청 내역"));

        // 1. 요청하신 컬럼 순서 설정 (학번 | 학생이름 | 강의명 | 교수명 | 학점)
        // ※ 주의: 수강 취소를 하려면 내부적으로 강의 고유 ID(lectureId)가 필요하므로,
        // 사용자 눈에 보이지 않게 0번 컬럼에 lectureId를 숨겨두거나 별도로 처리해야 합니다.
        String[] regCols = {"강의ID(숨김)", "학번", "학생이름", "강의명", "교수명", "학점"};

        DefaultTableModel regModel = new DefaultTableModel(regCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable regTable = new JTable(regModel);

        // 0번 컬럼(강의ID)은 화면에 보이지 않도록 너비를 0으로 숨깁니다.
        regTable.getColumnModel().getColumn(0).setMinWidth(0);
        regTable.getColumnModel().getColumn(0).setMaxWidth(0);
        regTable.getColumnModel().getColumn(0).setWidth(0);

        regPanel.add(new JScrollPane(regTable), BorderLayout.CENTER);

        JButton cancelBtn = new JButton("수강 취소");
        JButton refreshBtn = new JButton("새로고침"); // 👈 새로고침 버튼 생성

        JPanel btnP = new JPanel();
        btnP.add(cancelBtn);
        btnP.add(refreshBtn); // 👈 패널에 추가
        regPanel.add(btnP, BorderLayout.SOUTH);

        // 하단: 내 성적 조회
        JPanel scorePanel = new JPanel(new BorderLayout());
        scorePanel.setBorder(BorderFactory.createTitledBorder("내 성적 조회"));
        String[] scoreCols = {"성적ID", "학번", "이름", "강의코드", "강의명", "점수"};

        DefaultTableModel scoreModel = new DefaultTableModel(scoreCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable scoreTable = new JTable(scoreModel);
        scorePanel.add(new JScrollPane(scoreTable), BorderLayout.CENTER);

        // 데이터 조회 및 채우기
        Runnable loadMyData = () -> {
            regModel.setRowCount(0);
            scoreModel.setRowCount(0);

            // 2. 수강신청 내역 로드 및 매핑
            List<Registration> regs = registrationService.getMyLectureList(loginUser.getMemberId());
            if (regs != null) {
                for (Registration r : regs) {
                    regModel.addRow(new Object[]{
                            r.getLectureId(),     // 0번: 취소 처리를 위한 숨겨진 강의 ID[cite: 3]
                            r.getMemberId(),      // 1번: 학번[cite: 3]
                            r.getMemberName(),    // 2번: 학생이름[cite: 3]
                            r.getLectureName(),   // 3번: 강의명[cite: 3]
                            r.getProfessor(),     // 4번: 교수명[cite: 3]
                            r.getCredit()         // 5번: 학점[cite: 3]
                    });
                }
            }

            try {
                List<Scores> scores = scoreService.getScoresById(loginUser.getMemberId());
                for (Scores s : scores) {
                    scoreModel.addRow(new Object[]{s.getId(), s.getMemberId(), s.getName(), s.getLectureCode(), s.getLectureName(), s.getScore()});
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        };
        loadMyData.run();

        // 3. 수강 취소 버튼 이벤트 (숨겨둔 0번 인덱스에서 강의 ID를 가져와 취소 요청)
        cancelBtn.addActionListener(e -> {
            int row = regTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "취소할 강의를 선택하세요.");
                return;
            }
            int lecId = (int) regModel.getValueAt(row, 0); // 숨겨진 강의 ID 가져오기
            try {
                registrationService.deleteRegistration(String.valueOf(loginUser.getId()), String.valueOf(lecId));
                JOptionPane.showMessageDialog(this, "수강 취소되었습니다.");
                loadMyData.run();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "수강 취소 실패: " + ex.getMessage());
            }
        });
        refreshBtn.addActionListener(e -> {
            loadMyData.run(); // 👈 최신 내역 다시 불러오기
            JOptionPane.showMessageDialog(this, "목록이 새로고침되었습니다.");
        });

        panel.add(regPanel);
        panel.add(scorePanel);
        return panel;
    }

    // 3. 내 학적 정보 패널 (학생용)
    private JPanel createStudentProfilePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JPanel infoPanel = new JPanel(new GridLayout(5, 2, 15, 12));
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(180, 180, 180)), " 내 학적 정보 "),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        infoPanel.setBackground(Color.WHITE);

        JLabel idLabel = new JLabel();
        JLabel nameLabel = new JLabel();
        JLabel phoneLabel = new JLabel();
        JLabel majorLabel = new JLabel();
        JLabel gradeLabel = new JLabel();

        Font labelFont = new Font("SansSerif", Font.BOLD, 13);
        JLabel[] keys = {new JLabel("학번 (ID):"), new JLabel("이름:"), new JLabel("전화번호:"), new JLabel("학과:"), new JLabel("학년:")};
        for (JLabel k : keys) { k.setFont(labelFont); }

        infoPanel.add(keys[0]); infoPanel.add(idLabel);
        infoPanel.add(keys[1]); infoPanel.add(nameLabel);
        infoPanel.add(keys[2]); infoPanel.add(phoneLabel);
        infoPanel.add(keys[3]); infoPanel.add(majorLabel);
        infoPanel.add(keys[4]); infoPanel.add(gradeLabel);

        JPanel pwdPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        pwdPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(180, 180, 180)), " 비밀번호 변경 "),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        pwdPanel.setBackground(Color.WHITE);

        JPasswordField newPwdField = new JPasswordField(15);
        JButton changePwdBtn = new JButton("변경하기");

        pwdPanel.add(new JLabel("새 비밀번호 (8자 이상, 특수문자 포함):"));
        pwdPanel.add(newPwdField);
        pwdPanel.add(changePwdBtn);

        Runnable loadProfile = () -> {
            try {
                Members m = memberService.getSelfInfoById(loginUser.getId());
                if (m != null) {
                    idLabel.setText(m.getMemberId());
                    nameLabel.setText(m.getName());
                    phoneLabel.setText(m.getPhone() != null ? formatPhoneNumber(m.getPhone()) : "-");
                    majorLabel.setText(m.getMajor() != null ? m.getMajor() : "-");
                    gradeLabel.setText(m.getGrade() + "학년");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "정보를 불러오는데 실패했습니다: " + e.getMessage());
            }
        };
        loadProfile.run();

        changePwdBtn.addActionListener(e -> {
            String newPwd = new String(newPwdField.getPassword()).trim();
            try {
                boolean success = memberService.updateMemberPassword(loginUser.getId(), newPwd);
                if (success) {
                    JOptionPane.showMessageDialog(this, "비밀번호가 성공적으로 변경되었습니다.");
                    newPwdField.setText("");
                } else {
                    JOptionPane.showMessageDialog(this, "비밀번호 변경에 실패했습니다.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "오류: " + ex.getMessage());
            }
        });

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(infoPanel, gbc);

        gbc.gridy = 1;
        panel.add(pwdPanel, gbc);

        panel.setBackground(new Color(245, 246, 248));
        return panel;
    }

    // 4. [관리자] 강의 관리 패널
    private JPanel createAdminLecturePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel formPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 10));

        JTextField codeF = new JTextField(5);
        JTextField nameF = new JTextField(7);
        JTextField profF = new JTextField(5);
        JTextField creditF = new JTextField(2);
        JTextField capF = new JTextField(2);

        JButton addBtn = new JButton("강의 등록");
        JButton updateBtn = new JButton("강의 수정");
        JButton delBtn = new JButton("선택 강의 삭제");

        formPanel.add(new JLabel("코드:")); formPanel.add(codeF);
        formPanel.add(new JLabel("명칭:")); formPanel.add(nameF);
        formPanel.add(new JLabel("교수:")); formPanel.add(profF);
        formPanel.add(new JLabel("학점:")); formPanel.add(creditF);
        formPanel.add(new JLabel("정원:")); formPanel.add(capF);
        formPanel.add(addBtn);
        formPanel.add(updateBtn);
        formPanel.add(delBtn);

        panel.add(formPanel, BorderLayout.NORTH);

        String[] cols = {"ID", "강의코드", "강의명", "교수", "학점", "정원"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(model);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        Runnable loadAdminLectures = () -> {
            model.setRowCount(0);
            try {
                List<Lectures> list = lecturesService.getAllLectures();
                for (Lectures l : list) {
                    model.addRow(new Object[]{l.getId(), l.getLectureCode(), l.getLectureName(), l.getProfessor(), l.getCredit(), l.getCapacity()});
                }
            } catch (SQLException e) { e.printStackTrace(); }
        };
        loadAdminLectures.run();

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = table.getSelectedRow();
                if (row != -1) {
                    codeF.setText(model.getValueAt(row, 1).toString());
                    nameF.setText(model.getValueAt(row, 2).toString());
                    profF.setText(model.getValueAt(row, 3) != null ? model.getValueAt(row, 3).toString() : "");
                    creditF.setText(model.getValueAt(row, 4).toString());
                    capF.setText(model.getValueAt(row, 5).toString());
                }
            }
        });

        addBtn.addActionListener(e -> {
            try {
                Lectures l = Lectures.builder()
                        .lectureCode(codeF.getText().trim())
                        .lectureName(nameF.getText().trim())
                        .professor(profF.getText().trim())
                        .credit(Integer.parseInt(creditF.getText().trim()))
                        .capacity(Integer.parseInt(capF.getText().trim()))
                        .available(true)
                        .build();
                lecturesService.addLectures(l);
                JOptionPane.showMessageDialog(this, "강의가 등록되었습니다.");
                loadAdminLectures.run();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "등록 실패: " + ex.getMessage());
            }
        });

        updateBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "수정할 강의를 선택하세요.");
                return;
            }
            try {
                Lectures l = Lectures.builder()
                        .lectureCode(codeF.getText().trim())
                        .lectureName(nameF.getText().trim())
                        .professor(profF.getText().trim())
                        .credit(Integer.parseInt(creditF.getText().trim()))
                        .capacity(Integer.parseInt(capF.getText().trim()))
                        .build();

                lecturesService.updateLectures(l);
                JOptionPane.showMessageDialog(this, "강의 정보가 수정되었습니다.");
                loadAdminLectures.run();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "수정 실패: " + ex.getMessage());
            }
        });

        delBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "삭제할 강의를 선택하세요.");
                return;
            }
            String code = (String) model.getValueAt(row, 1);
            int confirm = JOptionPane.showConfirmDialog(this, "정말 이 강의를 삭제하시겠습니까?", "강의 삭제", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (lecturesService.deleteLectures(code)) {
                    JOptionPane.showMessageDialog(this, "삭제되었습니다.");
                    loadAdminLectures.run();
                } else {
                    JOptionPane.showMessageDialog(this, "삭제 실패 (수강생 존재 등)");
                }
            }
        });

        return panel;
    }

    // 5. [관리자] 성적 관리 패널
    private JPanel createAdminScorePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel scoreForm = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 10));

        JTextField memIdF = new JTextField(7);
        JTextField lecCodeF = new JTextField(7);
        JTextField scoreF = new JTextField(4);

        JButton addBtn = new JButton("성적 등록");
        JButton updateBtn = new JButton("성적 수정");
        JButton delBtn = new JButton("성적 삭제");

        scoreForm.add(new JLabel("학생ID(학번):")); scoreForm.add(memIdF);
        scoreForm.add(new JLabel("강의코드:")); scoreForm.add(lecCodeF);
        scoreForm.add(new JLabel("점수:")); scoreForm.add(scoreF);
        scoreForm.add(addBtn);
        scoreForm.add(updateBtn);
        scoreForm.add(delBtn);

        panel.add(scoreForm, BorderLayout.NORTH);

        String[] cols = {"ID", "학번", "이름", "강의코드", "강의명", "점수"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(model);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        Runnable loadAllScores = () -> {
            model.setRowCount(0);
            try {
                List<Scores> list = scoreService.getAllScores();
                list.sort(Comparator.comparing(Scores::getMemberId));

                for (Scores s : list) {
                    model.addRow(new Object[]{s.getId(), s.getMemberId(), s.getName(), s.getLectureCode(), s.getLectureName(), s.getScore()});
                }
            } catch (SQLException e) { e.printStackTrace(); }
        };
        loadAllScores.run();

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = table.getSelectedRow();
                if (row != -1) {
                    memIdF.setText(model.getValueAt(row, 1).toString());
                    lecCodeF.setText(model.getValueAt(row, 3).toString());
                    scoreF.setText(model.getValueAt(row, 5).toString());
                }
            }
        });

        addBtn.addActionListener(e -> {
            try {
                String mId = memIdF.getText().trim();
                String lCode = lecCodeF.getText().trim();
                int scoreVal = Integer.parseInt(scoreF.getText().trim());

                List<Scores> currentList = scoreService.getAllScores();
                boolean alreadyExists = currentList.stream()
                        .anyMatch(s -> s.getMemberId().equals(mId) && s.getLectureCode().equals(lCode));

                if (alreadyExists) {
                    JOptionPane.showMessageDialog(this, "이미 해당 학생의 성적이 존재합니다. 등록 대신 [수정]을 이용해 주세요.");
                    return;
                }

                scoreService.addScore(mId, lCode);
                scoreService.updateScore(mId, lCode, scoreVal);

                JOptionPane.showMessageDialog(this, "성적이 등록되었습니다.");
                loadAllScores.run();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "등록 실패: " + ex.getMessage());
            }
        });

        updateBtn.addActionListener(e -> {
            try {
                String mId = memIdF.getText().trim();
                String lCode = lecCodeF.getText().trim();
                int scoreVal = Integer.parseInt(scoreF.getText().trim());

                scoreService.updateScore(mId, lCode, scoreVal);

                JOptionPane.showMessageDialog(this, "성적이 수정되었습니다.");
                loadAllScores.run();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "수정 실패: " + ex.getMessage());
            }
        });

        delBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "삭제할 성적 행을 선택하세요.");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this, "정말 이 성적 정보를 삭제하시겠습니까?", "성적 삭제", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    String memberId = model.getValueAt(row, 1).toString();
                    String lectureCode = model.getValueAt(row, 3).toString();

                    scoreService.deleteScore(memberId, lectureCode);

                    JOptionPane.showMessageDialog(this, "성적이 삭제되었습니다.");
                    loadAllScores.run();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "삭제 실패: " + ex.getMessage());
                }
            }
        });

        return panel;
    }

    // 6. [관리자] 학생 정보 관리 패널 (학번/이름 검색 기능 포함)
    private JPanel createAdminMemberPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // 상단 컨테이너 (검색 바 + 등록/수정 폼 통합)
        JPanel northPanel = new JPanel(new BorderLayout());

        // 검색 바 패널
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JComboBox<String> searchTypeCombo = new JComboBox<>(new String[]{"이름 검색", "학번 검색"});
        JTextField searchField = new JTextField(12);
        JButton searchBtn = new JButton("검색");
        JButton allBtn = new JButton("전체보기");

        searchPanel.add(searchTypeCombo);
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        searchPanel.add(allBtn);
        northPanel.add(searchPanel, BorderLayout.NORTH);

        // 입력 폼 패널
        JPanel formPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 10));
        JTextField memberIdF = new JTextField(6);
        JPasswordField passwordF = new JPasswordField(6);
        JTextField nameF = new JTextField(5);
        JTextField phoneF = new JTextField(7);
        JTextField majorF = new JTextField(6);
        JTextField gradeF = new JTextField(2);

        JButton addBtn = new JButton("학생 등록");
        JButton updateBtn = new JButton("학생 정보 수정");
        JButton delBtn = new JButton("학생 삭제");

        formPanel.add(new JLabel("학번:")); formPanel.add(memberIdF);
        formPanel.add(new JLabel("비번:")); formPanel.add(passwordF);
        formPanel.add(new JLabel("이름:")); formPanel.add(nameF);
        formPanel.add(new JLabel("전화번호:")); formPanel.add(phoneF);
        formPanel.add(new JLabel("학과:")); formPanel.add(majorF);
        formPanel.add(new JLabel("학년:")); formPanel.add(gradeF);
        formPanel.add(addBtn);
        formPanel.add(updateBtn);
        formPanel.add(delBtn);

        northPanel.add(formPanel, BorderLayout.SOUTH);
        panel.add(northPanel, BorderLayout.NORTH);

        // 테이블 구성
        String[] cols = {"PK(ID)", "학번", "이름", "전화번호", "학과", "학년"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(model);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        Runnable loadMembers = () -> {
            model.setRowCount(0);
            try {
                List<Members> list = memberService.getAllMembers();
                if (list != null) {
                    for (Members m : list) {
                        model.addRow(new Object[]{
                                m.getId(), m.getMemberId(), m.getName(),
                                formatPhoneNumber(m.getPhone()), m.getMajor(), m.getGrade()
                        });
                    }
                }
            } catch (Exception e) { e.printStackTrace(); }
        };
        loadMembers.run();

        // 검색 버튼 이벤트
        searchBtn.addActionListener(e -> {
            String keyword = searchField.getText().trim();
            if (keyword.isEmpty()) {
                JOptionPane.showMessageDialog(this, "검색어를 입력하세요.");
                return;
            }
            model.setRowCount(0);
            try {
                String type = (String) searchTypeCombo.getSelectedItem();
                if ("이름 검색".equals(type)) {
                    List<Members> list = memberService.getMembersByName(keyword);
                    for (Members m : list) {
                        model.addRow(new Object[]{m.getId(), m.getMemberId(), m.getName(), m.getPhone(), m.getMajor(), m.getGrade()});
                    }
                } else {
                    Members m = memberService.getMembersById(keyword);
                    if (m != null) {
                        model.addRow(new Object[]{m.getId(), m.getMemberId(), m.getName(), m.getPhone(), m.getMajor(), m.getGrade()});
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "검색 결과가 없습니다: " + ex.getMessage());
            }
        });

        allBtn.addActionListener(e -> {
            searchField.setText("");
            loadMembers.run();
        });

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = table.getSelectedRow();
                if (row != -1) {
                    memberIdF.setText(model.getValueAt(row, 1) != null ? model.getValueAt(row, 1).toString() : "");
                    nameF.setText(model.getValueAt(row, 2) != null ? model.getValueAt(row, 2).toString() : "");
                    phoneF.setText(model.getValueAt(row, 3) != null ? model.getValueAt(row, 3).toString() : "");
                    majorF.setText(model.getValueAt(row, 4) != null ? model.getValueAt(row, 4).toString() : "");
                    gradeF.setText(model.getValueAt(row, 5) != null ? model.getValueAt(row, 5).toString() : "");
                }
            }
        });

        addBtn.addActionListener(e -> {
            try {
                String mId = memberIdF.getText().trim();
                String pwd = new String(passwordF.getPassword()).trim();
                String name = nameF.getText().trim();
                String phone = phoneF.getText().trim();
                String major = majorF.getText().trim();
                int grade = Integer.parseInt(gradeF.getText().trim());

                memberService.registerMember(mId, pwd, name, phone, major, grade);
                JOptionPane.showMessageDialog(this, "학생이 성공적으로 등록되었습니다.");
                loadMembers.run();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "등록 실패: " + ex.getMessage());
            }
        });

        updateBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "수정할 학생 행을 선택하세요.");
                return;
            }
            try {
                int targetPk = (int) model.getValueAt(row, 0);
                String newName = nameF.getText().trim();
                String newPhone = phoneF.getText().trim();
                String newMajor = majorF.getText().trim();

                memberService.updateMemberName(targetPk, newName);
                memberService.updateMemberPhone(targetPk, newPhone);
                memberService.updateMemberMajor(targetPk, newMajor);

                JOptionPane.showMessageDialog(this, "학생 정보가 수정되었습니다.");
                loadMembers.run();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "수정 실패: " + ex.getMessage());
            }
        });

        delBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "삭제할 학생 행을 선택하세요.");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this, "정말 이 학생 정보를 삭제하시겠습니까?", "학생 삭제", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    int targetPk = (int) model.getValueAt(row, 0);
                    memberService.deleteMember(targetPk);

                    JOptionPane.showMessageDialog(this, "학생이 삭제되었습니다.");
                    loadMembers.run();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "삭제 실패: " + ex.getMessage());
                }
            }
        });

        return panel;
    }

    // 7. [관리자] 전체 수강신청 현황 조회 패널
    private JPanel createAdminRegistrationPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel topP = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshBtn = new JButton("새로고침");
        topP.add(refreshBtn);
        panel.add(topP, BorderLayout.NORTH);

        String[] cols = {"회원ID(PK)", "학생이름", "강의ID", "강의명"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(model);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        Runnable loadAllRegistrations = () -> {
            model.setRowCount(0);
            try {
                List<Registration> list = registrationService.getAllLectureList(loginUser);
                if (list != null) {
                    for (Registration r : list) {
                        model.addRow(new Object[]{
                                r.getMemberId(), r.getMemberName(),
                                r.getLectureId(), r.getLectureName()
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        loadAllRegistrations.run();

        refreshBtn.addActionListener(e -> loadAllRegistrations.run());

        return panel;
    }
    // 전화번호 형식 변환 메서드 (예: 01011112222 -> 010-1111-2222)
    private String formatPhoneNumber(String phone) {
        if (phone == null) return "-";
        phone = phone.replaceAll("[^0-9]", ""); // 숫자만 추출
        if (phone.length() == 11) {
            return phone.replaceFirst("^(\\d{3})(\\d{4})(\\d{4})$", "$1-$2-$3");
        } else if (phone.length() == 10) {
            return phone.replaceFirst("^(\\d{3})(\\d{3})(\\d{4})$", "$1-$2-$3");
        }
        return phone; // 형식이 맞지 않으면 원본 반환
    }
}