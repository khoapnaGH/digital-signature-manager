/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.khoaluantotnghiep.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author Admin
 */
public class DaoFactoryUser {
    private String rootFolder = System.getProperty("user.dir");
    private final String DB_NAME = "digitalsignature.db";
    private final String URL = "jdbc:sqlite:"+rootFolder+"\\"+DB_NAME;
    
    public Connection getConnection(){
        
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }
}
