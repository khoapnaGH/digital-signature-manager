/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.khoaluantotnghiep.controller;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author Admin
 */
public class Helper {
        
    public String randomSerial(){
       String result = new SecureRandom().ints(0,36)
            .mapToObj(i -> Integer.toString(i, 36))
            .map(String::toUpperCase).distinct().limit(16).collect(Collectors.joining())
            .replaceAll("([A-Z0-9]{4})", "$1-").substring(0,19);
       return result;
    }
    
    public String getCurrentDate(){
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        Date date = new Date();
        return formatter.format(date.getTime());
    }
    
    public static boolean deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (null != files) {
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isDirectory()) {
                        deleteDirectory(files[i]);
                    } else {
                        files[i].delete();
                    }
                }
            }
        }
        return (directory.delete());
    }
    
    public void createDir(String dir) {
        File f = new File(dir);
        if (!f.exists()) {
            f.mkdir();
        }
    }

    public boolean Move(String oldF, String newF) {
        File sourceFile = new File(oldF);
        File destFile = new File(newF);

        if (sourceFile.renameTo(destFile)) {
            System.out.println("File renamed successfully");
            return true;
        } else {
            System.out.println("Failed to rename file");
            return false;
        }
    }
    
    public boolean Copy(String srcFilePath, String destFilePath){
        File sourceFile = new File(srcFilePath);
        File destFile = new File(destFilePath);
        if(destFile.exists())
            destFile.delete();
        try {
            Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException ex) {
            Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    public String getJarFileBuilded(String folder){
        File f = new File(folder);
        if(!f.exists())
            return "";
        File[] files = f.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith("with-dependencies.jar");
            }
        });
        if(files == null || files.length == 0)
            return "";
        return files[0].getAbsolutePath();
    }
    
    public void clearExe(String folder){
        File fd = new File(folder);
        File[] files = fd.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".exe");
            }
        });
        for(File f : files){
            f.delete();
        }
    }
    
    public String getExe(String folder){
        File fd = new File(folder);
        File[] files = fd.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".exe");
            }
        });
        return files[0].getAbsolutePath();
    }
}
