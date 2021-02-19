/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.khoaluantotnghiep.dao;

import com.khoaluantotnghiep.model.ModelUserInfomation;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Admin
 */
public class AdminDAO implements IAdmin {

    private static final String INSERT_USER = "INSERT INTO users(serial, name, organization, address, active, password, email, note) "
            + "VALUES(?,?,?,?,?,?,?,?)";

    private static final String UPDATE_PASSWORD = "UPDATE users SET password = ? WHERE serial = ?";

    private static final String UPDATE_INFO = "UPDATE users"
            + " SET name = ?,"
            + "organization = ?,"
            + "address = ?,"
            + "email = ?,"
            + "note = ?"
            + "WHERE serial = ?";

    private static final String DELETE_USER = "DELETE FROM users WHERE serial = ?";

    private static final String GET_INFO = "SELECT *FROM users";

    private DaoFactoryAdmin dao;

    public AdminDAO() {
        dao = new DaoFactoryAdmin();
    }

    private ModelUserInfomation getInfo(ResultSet rset) {
        ModelUserInfomation ui = null;
        try {
            ui = new ModelUserInfomation(rset.getString("serial"),
                    rset.getString("name"),
                    rset.getString("email"),
                    rset.getString("organization"),
                    rset.getString("address"),
                    rset.getString("active"),
                    rset.getString("password"),
                    rset.getString("note"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ui;
    }

    // serial, name, organization, address, active, password, email, note
    @Override
    public boolean insertNewUser(ModelUserInfomation urInfo) {
        try {
            Connection conn = dao.getConnection();

            PreparedStatement stm = conn.prepareStatement(INSERT_USER);

            stm.setString(1, urInfo.getSerial());
            stm.setString(2, urInfo.getUserFullName());
            stm.setString(3, urInfo.getOrganization());
            stm.setString(4, urInfo.getAddress());
            stm.setString(5, urInfo.getActiveDate());
            stm.setString(6, urInfo.getPassword());
            stm.setString(7, urInfo.getEmailContact());
            stm.setString(8, urInfo.getNote());
            stm.executeUpdate();

            stm.close();
            conn.close();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updatePasswordAnUser(String serial, String passwordBcrypted) {
        try {
            Connection conn = dao.getConnection();

            PreparedStatement stm = conn.prepareStatement(UPDATE_PASSWORD);

            stm.setString(1, passwordBcrypted);
            stm.setString(2, serial);

            stm.executeUpdate();

            stm.close();
            conn.close();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    /*
        private static final String UPDATE_INFO = "UPDATE users"
            + " SET name = ?,"
            + "organization = ?,"
            + "address = ?,"
            + "email = ?,"
            + "note = ?"
            + "WHERE serial = ?";
    */
    
    @Override
    public boolean updateInfomationAnUser(ModelUserInfomation urInfo) {
            try {
            Connection conn = dao.getConnection();
            
            PreparedStatement stm = conn.prepareStatement(UPDATE_INFO);
            
            stm.setString(1, urInfo.getUserFullName());
            stm.setString(2, urInfo.getOrganization());
            stm.setString(3, urInfo.getAddress());
            stm.setString(4, urInfo.getEmailContact());
            stm.setString(5, urInfo.getNote());
            stm.setString(6, urInfo.getSerial());
            
            stm.executeUpdate();
            
            stm.close();
            conn.close();
            
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteUser(String serial) {
        try {
            Connection conn = dao.getConnection();
            
            PreparedStatement stm = conn.prepareStatement(DELETE_USER);
            
            stm.setString(1, serial);
            
            stm.executeUpdate();
            
            stm.close();
            conn.close();
            
            return true;
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<ModelUserInfomation> getAllUsers() {
        List<ModelUserInfomation> listUsers = new ArrayList<>();
        try {
            Connection conn = dao.getConnection();
            
            PreparedStatement stm = conn.prepareStatement(GET_INFO);
            
            ResultSet rset = stm.executeQuery();
            
            while(rset.next()){
                listUsers.add(getInfo(rset));
            }
            
            stm.close();
            conn.close();       
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listUsers;
    }

}
