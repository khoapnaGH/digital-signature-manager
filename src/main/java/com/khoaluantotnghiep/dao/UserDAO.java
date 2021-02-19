/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.khoaluantotnghiep.dao;

import com.khoaluantotnghiep.model.ModelUserInfomation;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 *
 * @author Admin
 */
public class UserDAO implements IUser {

    DaoFactoryUser dao;

    private static final String DELETE_KEY = "DELETE FROM secure";

    private static final String DELETE_USER = "DELETE FROM users";

    private static final String INSERT_USER = "INSERT INTO users(serial, name, organization, address, active, password, email) "
            + "VALUES(?,?,?,?,?,?,?)";

    private static final String INSERT_KEY = "INSERT INTO secure(key) VALUES(?)";

    public UserDAO() {
        this.dao = new DaoFactoryUser();
    }

    @Override
    public boolean insertKey(String key) {
        try {
            Connection conn = dao.getConnection();

            PreparedStatement stm = conn.prepareStatement(INSERT_KEY);

            stm.setString(1, key);
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
    public boolean insetUser(ModelUserInfomation urInfo) {
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
    public void emptyDatabase() {
        emptySecure();
        emptyUser();
    }

    private void emptySecure() {
        try {
            Connection conn = dao.getConnection();

            PreparedStatement stm = conn.prepareStatement(DELETE_KEY);

            stm.executeUpdate();

            stm.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void emptyUser() {
        try {
            Connection conn = dao.getConnection();

            PreparedStatement stm = conn.prepareStatement(DELETE_USER);

            stm.executeUpdate();

            stm.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
