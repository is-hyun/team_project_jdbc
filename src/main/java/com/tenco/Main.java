package com.tenco;

import com.tenco.dao.ScoreDAO;
import com.tenco.dto.Scores;
import com.tenco.view.LmsView;

import com.tenco.dao.MembersDAO;
import com.tenco.dao.RegistrationDAO;
import com.tenco.dto.Members;
import com.tenco.dto.Registration;
import com.tenco.dto.SearchMembersByIdDTO;

import java.util.List;

public class Main {
    public static void main(String[] args) throws SQLException {
        LmsView lmsView = new LmsView();
        lmsView.start();
    }
}
