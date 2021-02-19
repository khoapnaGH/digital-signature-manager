/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.khoaluantotnghiep.controller;

import com.khoaluantotnghiep.dao.AdminDAO;
import com.khoaluantotnghiep.dao.UserDAO;
import com.khoaluantotnghiep.model.AES;
import com.khoaluantotnghiep.model.ModelUserInfomation;
import com.khoaluantotnghiep.model.ReadStream;
import com.khoaluantotnghiep.view.MainFrame;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.swing.JOptionPane;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.mindrot.jbcrypt.BCrypt;

/**
 *
 * @author Admin
 */
public class DigitalSignatrueManagementController {

    private static MainFrame frame = null;

    private static AdminDAO userDAO = null;

    private static List<ModelUserInfomation> listUsers = null;
    private static Helper helper = null;
    private int indexTable = 1;

    private static final String salt = "qU4n6_.Kh04";
    private static final int bcryptRound = 12;

    private static final String ROOT_PATH = System.getProperty("user.dir");
    private static final String MVN_PATH = ROOT_PATH + "\\apache-maven-3.6.3\\bin\\mvn.cmd";
    private static final String PROJECT_NAME = "digital-signature";
    private static final String PROJECT_PATH = ROOT_PATH + "\\" + PROJECT_NAME;
    private static final String BUILDED_PATH = PROJECT_PATH + "\\target";
    private static final String PUBLIC_KEY_FILE = PROJECT_PATH + "\\src\\main\\resources\\keys\\public.rsa";
    private static final String PRIVATE_KEY_FILE = PROJECT_PATH + "\\src\\main\\resources\\keys\\private.rsa";
    private static final String DATABASE_FILE = ROOT_PATH + "\\digitalsignature.db";

    public DigitalSignatrueManagementController(MainFrame frame) {
        this.frame = frame;
        listUsers = new ArrayList<>();
        helper = new Helper();
        userDAO = new AdminDAO();
        initView();
        initEventHandle();
    }

    private void initView() {
        this.frame.getTableUsers().getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        this.frame.getTableUsers().getTableHeader().setForeground(new Color(149, 165, 166));
        this.frame.getTableUsers().getColumnModel().getColumn(0).setPreferredWidth(40);
        this.frame.getTableUsers().getColumnModel().getColumn(1).setPreferredWidth(120);
        this.frame.getTableUsers().getColumnModel().getColumn(2).setPreferredWidth(150);
        this.frame.getTableUsers().getColumnModel().getColumn(3).setPreferredWidth(120);
        this.frame.getTableUsers().getColumnModel().getColumn(4).setPreferredWidth(160);
        this.frame.getTableUsers().getColumnModel().getColumn(5).setPreferredWidth(160);
        this.frame.getTableUsers().getColumnModel().getColumn(6).setPreferredWidth(80);
        this.frame.getTableUsers().getColumnModel().getColumn(7).setPreferredWidth(160);

        loadUsers();
        lookupUser();

        this.frame.getTableUsers().addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent evt) {
                if (SwingUtilities.isRightMouseButton(evt)) {
                    frame.getPopupTableUser().show(evt.getComponent(), evt.getX(), evt.getY());
                }
            }
        });

        this.frame.getDialogAddNewUser().setLocationRelativeTo(this.frame);
        this.frame.getDialogEditUser().setLocationRelativeTo(this.frame);
        this.frame.getDialogCreatePassword().setLocationRelativeTo(this.frame);
        this.frame.getDialogPackSoftware().setLocationRelativeTo(this.frame);
    }

    private void initEventHandle() {
        //Click button add a new user
        this.frame.getBtnAddNewUser().addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent evt) {
                resetDialogAddNewUser();
                frame.getDialogAddNewUser().setVisible(true);
            }
        });

        this.frame.getDlAddNewUser_Add().addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent evt) {
                handleAddNewUser();
            }
        });

        this.frame.getMenuItemRemoveUser().addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent evt) {
                frame.getPopupTableUser().setVisible(false);
                handleRemoveUser();
            }
        });

        this.frame.getMenuItemEditUser().addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent evt) {
                handleEditUser();
            }
        });

        this.frame.getDlBtnSave().addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent evt) {
                handleSaveUserEdit();
            }
        });

        this.frame.getBtnCreatePass().addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent evt) {
                handleOpenCreatePassword();
            }
        });

        this.frame.getBtnHash().addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent evt) {
                handleHashPassword();
            }
        });

        this.frame.getMenuItemPackSoftWare().addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent evt) {
                handleOpenBuild();
            }
        });

        this.frame.getDlBtnBuild().addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent evt) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        handleBuild();
                    }
                }).start();
            }
        });
    }

    private void resetDialogAddNewUser() {
        this.frame.getDlAddNewUser_Address().setText("");
        this.frame.getDlAddNewUser_FullName().setText("");
        this.frame.getDlAddNewUser_Note().setText("");
        this.frame.getDlAddNewUser_Organization().setText("");
        this.frame.getDlAddNewUser_Email().setText("");
    }

    private ModelUserInfomation createNewUser() {
        String serial = helper.randomSerial();
        String fullName = this.frame.getDlAddNewUser_FullName().getText();
        String organization = this.frame.getDlAddNewUser_Organization().getText();
        String note = this.frame.getDlAddNewUser_Note().getText();
        String address = this.frame.getDlAddNewUser_Address().getText();
        String actived = helper.getCurrentDate();
        String email = this.frame.getDlAddNewUser_Email().getText();
        if (email.trim().isEmpty()) {
            JOptionPane.showMessageDialog(frame.getDialogAddNewUser(),
                    "Email and password shouldn't empty",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        ModelUserInfomation user = new ModelUserInfomation(serial,
                fullName,
                email,
                organization,
                address,
                actived,
                "",
                note);
        return user;
    }

    private void lookupUser() {
        DefaultTableModel dtm = getUserTableModel();
        TableRowSorter<TableModel> rowSorter
                = new TableRowSorter<>(dtm);
        this.frame.getTableUsers().setRowSorter(rowSorter);
        this.frame.getTxtLookupUser().getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {

                String text = frame.getTxtLookupUser().getText();
                System.out.println(text);
                if (text.trim().length() == 0) {
                    rowSorter.setRowFilter(null);
                } else {
                    rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                String text = frame.getTxtLookupUser().getText();

                if (text.trim().length() == 0) {
                    rowSorter.setRowFilter(null);
                } else {
                    rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });
    }

    private DefaultTableModel getUserTableModel() {
        return (DefaultTableModel) this.frame.getTableUsers().getModel();
    }

    private void fillUserToTalbe(ModelUserInfomation user) {
        DefaultTableModel dtm = getUserTableModel();
        dtm.addRow(new Object[]{indexTable,
            user.getSerial(),
            user.getUserFullName(),
            user.getEmailContact(),
            user.getOrganization(),
            user.getAddress(),
            user.getActivateDate(),
            user.getNote()});
        indexTable++;
    }

    private void editUserInTable(ModelUserInfomation user) {
        DefaultTableModel dtm = getUserTableModel();
        for (int i = 0; i < dtm.getRowCount(); i++) {
            if (String.valueOf(dtm.getValueAt(i, 1)).equals(user.getSerial())) {
                dtm.setValueAt(user.getUserFullName(), i, 2);
                dtm.setValueAt(user.getEmailContact(), i, 3);
                dtm.setValueAt(user.getOrganization(), i, 4);
                dtm.setValueAt(user.getAddress(), i, 5);
                dtm.setValueAt(user.getNote(), i, 7);
                return;
            }
        }
    }

    private void fillUserToEditDialog(ModelUserInfomation user) {
        this.frame.getDlSerial().setText(user.getSerial());
        this.frame.getDlFullName().setText(user.getUserFullName());
        this.frame.getDlEmail().setText(user.getEmailContact());
        this.frame.getDlOrganization().setText(user.getOrganization());
        this.frame.getDlNote().setText(user.getNote());
        this.frame.getDlAddress().setText(user.getAddress());
    }

    private void logsBuild(String msg) {
        this.frame.getLogsPack().append(msg + "\n");
    }

    private void resetIndex() {
        indexTable = 1;
        DefaultTableModel dtm = getUserTableModel();
        for (int i = 0; i < dtm.getRowCount(); i++) {
            dtm.setValueAt(indexTable, i, 0);
            indexTable++;
        }
    }

    private int getRowSelected() {
        return this.frame.getTableUsers().getSelectedRow();
    }

    private void loadUsers() {
        listUsers = userDAO.getAllUsers();
        listUsers.forEach(e -> {
            fillUserToTalbe(e);
        });
    }

    private ModelUserInfomation getUserFromSerial(String serial) {
        return listUsers.stream().filter(u -> u.getSerial().equals(serial)).collect(Collectors.toList()).get(0);
    }

    private void handleRemoveUser() {
        int rs = JOptionPane.showConfirmDialog(this.frame,
                "Do you wanna remove this user?",
                "Remove user",
                JOptionPane.YES_NO_OPTION);

        if (rs == JOptionPane.NO_OPTION) {
            return;
        }

        DefaultTableModel dtm = getUserTableModel();
        int indexRowSelected = getRowSelected();
        String serialSelected = String.valueOf(dtm.getValueAt(indexRowSelected, 1));

        boolean removeRs = userDAO.deleteUser(serialSelected);
        if (!removeRs) {
            JOptionPane.showMessageDialog(this.frame,
                    "Remove user failed",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        dtm.removeRow(indexRowSelected);
        listUsers.removeIf(user -> (user.getSerial().equals(serialSelected)));
        JOptionPane.showMessageDialog(this.frame,
                "Remove User Successfully!",
                "Successfully",
                JOptionPane.INFORMATION_MESSAGE);
        resetIndex();
    }

    private void handleAddNewUser() {
        ModelUserInfomation mUser = createNewUser();
        if (mUser == null) {
            return;
        }
        boolean saveRs = userDAO.insertNewUser(mUser);
        if (!saveRs) {
            JOptionPane.showMessageDialog(this.frame.getDialogAddNewUser(),
                    "Save user failed",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        listUsers.add(mUser);
        fillUserToTalbe(mUser);
        frame.getDialogAddNewUser().setVisible(false);
        JOptionPane.showMessageDialog(frame,
                "Add new user successfully",
                "Successfully",
                JOptionPane.INFORMATION_MESSAGE);

    }

    private void handleEditUser() {
        DefaultTableModel dtm = getUserTableModel();
        int indexRowSelected = getRowSelected();
        String serialSelected = String.valueOf(dtm.getValueAt(indexRowSelected, 1));
        ModelUserInfomation user = getUserFromSerial(serialSelected);
        fillUserToEditDialog(user);
        this.frame.getDialogEditUser().setVisible(true);
    }

    private void handleSaveUserEdit() {
        String serial = this.frame.getDlSerial().getText();
        String fullName = this.frame.getDlFullName().getText();
        String email = this.frame.getDlEmail().getText();
        String organization = this.frame.getDlOrganization().getText();
        String note = this.frame.getDlNote().getText();
        String address = this.frame.getDlAddress().getText();

        if (email.trim().isEmpty() || fullName.trim().isEmpty()) {
            JOptionPane.showMessageDialog(frame.getDialogEditUser(),
                    "Email and Full name shouldn't empty",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        for (ModelUserInfomation user : listUsers) {
            if (user.getSerial().equals(serial)) {
                user.setUserFullName(fullName);
                user.setEmailContact(email);
                user.setOrganization(organization);
                user.setNote(note);
                user.setAddress(address);
                boolean updateRs = userDAO.updateInfomationAnUser(user);
                if (!updateRs) {
                    JOptionPane.showMessageDialog(frame.getDialogEditUser(),
                            "Update user to database failed",
                            "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    editUserInTable(user);
                    JOptionPane.showMessageDialog(frame.getDialogEditUser(),
                            "Update user successfully",
                            "Successfully", JOptionPane.INFORMATION_MESSAGE);
                }

                this.frame.getDialogEditUser().setVisible(false);
                return;
            }
        }
    }

    private void handleOpenCreatePassword() {
        this.frame.getTxtBcrypt().setText("");
        this.frame.getTxtPlainTextPass().setText("");
        this.frame.getDialogCreatePassword().setVisible(true);
    }

    private void handleOpenBuild() {
        DefaultTableModel dtm = getUserTableModel();
        int indexRowSelected = getRowSelected();
        String serialSelected = String.valueOf(dtm.getValueAt(indexRowSelected, 1));
        this.frame.getDlPackTxtPassword().setText("");
        this.frame.getDlSerialPackLogs().setText(serialSelected);
        this.frame.getLogsPack().setText("");
        this.frame.getDialogPackSoftware().setVisible(true);
    }

    private void handleHashPassword() {
        String plaintextPass = this.frame.getTxtPlainTextPass().getText();
        String hash = BCrypt.hashpw(plaintextPass + salt, BCrypt.gensalt(bcryptRound));
        this.frame.getTxtBcrypt().setText(hash);
        JOptionPane.showMessageDialog(this.frame.getDialogCreatePassword(),
                "Hash password successfully!",
                "Hash successfully",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void exec(List<String> cmd) {
        String command = "";
        for (String c : cmd) {
            command = command + c + " ";
        }
        System.out.println(command);
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(new File(PROJECT_PATH));
        Process p = null;
        try {
            p = pb.start();
            ReadStream s1 = new ReadStream("stdin", p.getInputStream());
            ReadStream s2 = new ReadStream("stderr", p.getErrorStream());
            s1.start();
            s2.start();
            p.waitFor();
        } catch (IOException ex) {
            Logger.getLogger(DigitalSignatrueManagementController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(DigitalSignatrueManagementController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (p != null) {
                p.destroy();
            }
        }
    }

    private String buildProjectToJar() {
        logsBuild("Building project to jar file.......");
        List<String> cmd = new ArrayList<>();
        cmd.add(MVN_PATH);
        cmd.add("clean");
        cmd.add("install");
        exec(cmd);
        String jarFileBuilded = helper.getJarFileBuilded(BUILDED_PATH);
        if (jarFileBuilded.isEmpty()) {
            logsBuild("Build failed");
            return "";
        }
        logsBuild("Build successfully");
        logsBuild("Jar: " + jarFileBuilded);
        return jarFileBuilded;
    }

    private String jarToExe(String jarPath, String folderExportExe) {
        logsBuild("Convert jar to exe.....");
        List<String> cmd = new ArrayList<>();
        cmd.add("\"" + PROJECT_PATH + "\\Jar2Exe\\App\\Jar2Exe\\j2ewiz" + "\"");
        cmd.add("/jar");
        cmd.add("\"" + jarPath + "\"");
        cmd.add("/o");
        cmd.add("\"" + folderExportExe + "\\DigitalSignature.exe" + "\"");
        cmd.add("/m");
        cmd.add("com.digitalsignature.controller.App");
        cmd.add("/type");
        cmd.add("windows");
        cmd.add("/minjre");
        cmd.add("1.8");
        cmd.add("/platform");
        cmd.add("windows");
        cmd.add("/encrypt");
        cmd.add("/checksum");
        cmd.add("/amd64");
        cmd.add("/icon");
        cmd.add("\"" + ROOT_PATH + "\\icons8_hand_with_pen.ico, 0" + "\"");
        cmd.add("/pv");
        cmd.add("1,0,0,1");
        cmd.add("/fv");
        cmd.add("1,0,0,1");
        cmd.add("/ve");
        cmd.add("ProductVersion=1.0.0");
        cmd.add("/ve");
        cmd.add("\"ProductName=Digital Signature Software\"");
        cmd.add("/ve");
        cmd.add("\"LegalCopyright=Copyright (c) 2021\"");
        cmd.add("/ve");
        cmd.add("SpecialBuild=1.0.0");
        cmd.add("/ve");
        cmd.add("FileVersion=1.0.0");
        cmd.add("/ve");
        cmd.add("\"FileDescription=Digital Signature Software using sign and verify documents\"");
        cmd.add("/ve");
        cmd.add("\"LegalTrademarks=Trade marks\"");
        cmd.add("/ve");
        cmd.add("\"InternalName=1, 0, 0, 1\"");
        cmd.add("/ve");
        cmd.add("\"CompanyName=Tony Quang\"");
        cmd.add("/message");
        cmd.add("1=\"Java Runtime Environment not found. Please download jdk-8u211-windows-x64.exe\"");
        exec(cmd);
        logsBuild("Convert successfully");
        logsBuild("File exe: " + folderExportExe + "\\DigitalSignature.exe");
        return folderExportExe + "\\DigitalSignature.exe";
    }

    private void initDatabaseUser(String serial, String password, String AESKeyEncrypted) {
        logsBuild("Creating a new database........");
        ModelUserInfomation user = listUsers.stream()
                .filter(e -> e.getSerial().equals(serial))
                .collect(Collectors.toList())
                .get(0);
        user.setPassword(password);

        logsBuild("Insert a new user....");
        UserDAO urDAO = new UserDAO();
        urDAO.emptyDatabase();
        urDAO.insetUser(user);
        logsBuild("Inserting a new user successfully....");
        logsBuild("Inserting AES key....");
        urDAO.insertKey(AESKeyEncrypted);
        logsBuild("Insert AES key successfully....");
        logsBuild("Creating a new database successfully........");
    }

    /* generate Private key, public key files rsa and
    *  using public key encrypted AES key
    *  after that, move key pair to folder project digital-signature keys folder
    *  return AESKeyEncrypted
     */
    private String initKey() {
        try {
            logsBuild("-------GENRATE PUBLIC and PRIVATE KEY-------------");
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(4096); //1024 used for normal securities
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();

            //Pullingout parameters which makes up Key
            logsBuild("------- Creating Public key and Private key ----------");
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            RSAPublicKeySpec rsaPubKeySpec = keyFactory.getKeySpec(publicKey, RSAPublicKeySpec.class);
            RSAPrivateKeySpec rsaPrivKeySpec = keyFactory.getKeySpec(privateKey, RSAPrivateKeySpec.class);

            //Share public key with other so they can encrypt data and decrypt thoses using private key(Don't share with Other)
            logsBuild("--------SAVING PUBLIC KEY AND PRIVATE KEY TO FILES-------");
            saveKeys(PUBLIC_KEY_FILE, rsaPubKeySpec.getModulus(), rsaPubKeySpec.getPublicExponent());
            saveKeys(PRIVATE_KEY_FILE, rsaPrivKeySpec.getModulus(), rsaPrivKeySpec.getPrivateExponent());

            //Share public key with other so they can encrypt data and decrypt thoses using private key(Don't share with Other)
            logsBuild("--------SAVING PUBLIC KEY AND PRIVATE KEY TO FILES-------");
            saveKeys(PUBLIC_KEY_FILE, rsaPubKeySpec.getModulus(), rsaPubKeySpec.getPublicExponent());
            saveKeys(PRIVATE_KEY_FILE, rsaPrivKeySpec.getModulus(), rsaPrivKeySpec.getPrivateExponent());

            logsBuild("--------CREATE KEY SUCCESSFULLY--------");
            logsBuild("Generating AES key....");
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256); // for example
            SecretKey secretKey = keyGen.generateKey();

            //Encrypt Data using Public Key
            logsBuild("Encrypt AES key....");
            String encryptedData = encrypt(secretKey.toString(), publicKey);

            logsBuild("Generating AES key successfully....");
            return encryptedData;

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (IOException ex) {
            Logger.getLogger(DigitalSignatrueManagementController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }

    private void handleBuild() {
        String password = this.frame.getDlPackTxtPassword().getText();
        if (password.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this.frame.getDialogPackSoftware(),
                    "Password shouldn't empty",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String hash = BCrypt.hashpw(password + salt, BCrypt.gensalt(bcryptRound));
        this.frame.getTxtBcrypt().setText(hash);
        String AES = initKey();
        String serial = this.frame.getDlSerialPackLogs().getText();
        String folderExport = ROOT_PATH + "\\" + serial;
        helper.createDir(folderExport);
        initDatabaseUser(serial, hash, AES);
        logsBuild("Copy database file to " + ROOT_PATH + "\\" + serial);
        boolean rsCopy = helper.Copy(DATABASE_FILE, folderExport + "\\digitalsignature.db");
        if (!rsCopy) {
            logsBuild("Copy failed");
            return;
        }
        String jarFilePath = buildProjectToJar();
        if (jarFilePath.isEmpty()) {
            return;
        }
        helper.clearExe(PROJECT_PATH);
        String exeFile = jarToExe(jarFilePath, folderExport);
        String fExe = helper.getExe(PROJECT_PATH);
        helper.Move(fExe, folderExport+"\\DigitalSignature.exe");
        logsBuild("====================================");
        logsBuild("--------BUILD PROJECT SUCCESSFULLY---------");
    }

    private void saveKeys(String fileName, BigInteger mod, BigInteger exp) throws IOException {

        File f = new File(fileName);
        if (f.exists()) {
            f.delete();
        }

        FileOutputStream fos = null;
        ObjectOutputStream oos = null;

        try {
            fos = new FileOutputStream(fileName);
            oos = new ObjectOutputStream(new BufferedOutputStream(fos));

            oos.writeObject(mod);
            oos.writeObject(exp);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (oos != null) {
                oos.close();

                if (fos != null) {
                    fos.close();
                }
            }
        }
    }

    public String encrypt(String plaintext, PublicKey pubKey) {
        try {
            byte[] encryptedString = plaintext.getBytes();
            Cipher c = Cipher.getInstance("RSA");
            c.init(Cipher.ENCRYPT_MODE, pubKey);
            byte encryptOut[] = c.doFinal(encryptedString);
            String strEncrypt = Base64.getEncoder().encodeToString(encryptOut);
            return strEncrypt;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
