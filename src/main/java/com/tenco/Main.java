package com.tenco;

import com.tenco.view.LmsView;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException {
        LmsView lmsView = new LmsView();
        lmsView.start();
    }
}