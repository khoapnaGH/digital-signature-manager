/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.khoaluantotnghiep.digitalsignatureadminstrator;

import com.khoaluantotnghiep.controller.DigitalSignatrueManagementController;
import com.khoaluantotnghiep.view.MainFrame;

/**
 *
 * @author Admin
 */
public class App {
    public static void main(String[] args) {
        MainFrame mainFrame = new MainFrame();
        DigitalSignatrueManagementController c = new DigitalSignatrueManagementController(mainFrame);
        mainFrame.setVisible(true);
    }
}
