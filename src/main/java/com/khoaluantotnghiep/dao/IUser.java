/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.khoaluantotnghiep.dao;

import com.khoaluantotnghiep.model.ModelUserInfomation;

/**
 *
 * @author Admin
 */
public interface IUser {
    boolean insertKey(String key);
    boolean insetUser(ModelUserInfomation user);
    void emptyDatabase();
}
