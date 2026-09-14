package com.tenco;

import com.tenco.dao.ScoreDAO;
import com.tenco.dto.Scores;
import com.tenco.view.LmsView;

import java.sql.SQLException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws SQLException {
        LmsView lmsView = new LmsView();
        lmsView.start();
    }
}