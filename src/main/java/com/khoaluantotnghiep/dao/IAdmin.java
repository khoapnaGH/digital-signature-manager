/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.khoaluantotnghiep.dao;

import com.khoaluantotnghiep.model.ModelUserInfomation;
import java.util.List;



/**
 *
 * @author Admin
 */
public interface IAdmin {
    List<ModelUserInfomation> getAllUsers();
    boolean insertNewUser(ModelUserInfomation urInfo);
    boolean updatePasswordAnUser(String serial, String passwordBcrypted);
    boolean updateInfomationAnUser(ModelUserInfomation urInfo);
    boolean deleteUser(String serial);
}
