package com.yuzi.javaweb.homework2;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Objects;

public class HelloController {
  public RadioButton radioFile;
  public RadioButton radioFolder;
  public Button buttonChooseObject;
  public VBox root;
  public Label path;
  public PasswordField passWordField;
  @FXML
  private RadioButton radioEncrypt;
  @FXML
  private RadioButton radioDecrypt;
  ToggleGroup groupCryptType = new ToggleGroup();
  ToggleGroup groupObjectType = new ToggleGroup();

  @FXML
  private void initialize() {
    radioEncrypt.setToggleGroup(groupCryptType);
    radioDecrypt.setToggleGroup(groupCryptType);
    groupCryptType.selectedToggleProperty().addListener((observableValue, toggle, t1) -> path.setText("路径："));
    radioFile.setToggleGroup(groupObjectType);
    radioFolder.setToggleGroup(groupObjectType);
    groupObjectType.selectedToggleProperty().addListener((observableValue, toggle, t1) -> {
      if (t1 == radioFolder) {
        buttonChooseObject.setText("选择文件夹");
        path.setText("路径：");
      } else {
        buttonChooseObject.setText("选择文件");
        path.setText("路径：");
      }
    });
  }

  @FXML
  protected void onObjectButtonClick() {
    if (groupObjectType.getSelectedToggle() == radioFile) {
      FileChooser fileChooser = new FileChooser();
      fileChooser.setTitle("选择要操作的文件");
      if (groupCryptType.getSelectedToggle() == radioDecrypt) {
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("EnCrypted file", "*.enc"));
      }
      File selectedFile = fileChooser.showOpenDialog(root.getScene().getWindow());
      path.setText("路径：" + selectedFile.getPath());
    } else {
      DirectoryChooser directoryChooser = new DirectoryChooser();
      directoryChooser.setTitle("选择要操作的文件夹");
      File selectedFolder = directoryChooser.showDialog(root.getScene().getWindow());
      path.setText("路径：" + selectedFolder.getPath());
    }
  }

  @FXML
  protected void onExecuteButtonClick() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
    String objectPath = path.getText().replaceFirst("路径：", "");
    if (objectPath.equals("")) {
      ShowErr("没有选择文件或文件夹！");
      return;
    }
    String password = passWordField.getText();
    boolean res;
    if (groupCryptType.getSelectedToggle() == radioEncrypt) {
      if (groupObjectType.getSelectedToggle() == radioFile) {
        res = EncryptFile(new File(objectPath), new File(objectPath + ".enc"), password);
      } else {
        res = EncryptFolder(new File(objectPath), password);
      }
      if (res) {
        ShowInfo("加密成功！");
      } else {
        ShowErr("加密失败！");
      }
    } else {
      if (groupObjectType.getSelectedToggle() == radioFile) {
        res = DecryptFile(new File(objectPath), new File(objectPath.replace(".enc", "")), password);
      } else {
        res = DecryptFolder(new File(objectPath), password);
      }
      if (res) {
        ShowInfo("解密完成！");
      } else {
        ShowErr("解密失败！");
      }
    }
  }

  private Cipher initAESCipher(String password, int mode) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
    KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
    //keyGenerator.init(128, new SecureRandom(password.getBytes(StandardCharsets.UTF_8)));
    SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
    secureRandom.setSeed(password.getBytes(StandardCharsets.UTF_8));
    keyGenerator.init(secureRandom);
    SecretKey secretKey = keyGenerator.generateKey();
    byte[] codeFormat = secretKey.getEncoded();
    SecretKeySpec key = new SecretKeySpec(codeFormat, "AES");
    Cipher cipher = Cipher.getInstance("AES");
    // 初始化
    cipher.init(mode, key);
    return cipher;
  }

  private boolean EncryptFile(File sourceFile, File encryptFile, String password) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
    if (sourceFile.getPath().endsWith(".enc")) {
      ShowErr("该文件已被加密过！");
      return false;
    }
    InputStream inputStream = null;
    OutputStream outputStream = null;
    try {
      Cipher cipher = initAESCipher(password, Cipher.ENCRYPT_MODE);
      inputStream = new FileInputStream(sourceFile);
      outputStream = new FileOutputStream(encryptFile);
      CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, cipher);
      byte[] buffer = new byte[1024];
      int r;
      while ((r = inputStream.read(buffer)) >= 0) {
        cipherOutputStream.write(buffer, 0, r);
      }
      cipherOutputStream.close();
      sourceFile.delete();
      return true;
    } catch (IOException e) {
      e.printStackTrace();
      ShowErr(e.getMessage());
    } finally {
      try {
        assert inputStream != null;
        inputStream.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
      try {
        assert outputStream != null;
        outputStream.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return false;
  }

  private boolean EncryptFolder(File folder, String password) {
    File[] fileList = folder.listFiles();
    try {
      for (File file : Objects.requireNonNull(fileList)) {
        if (file.isDirectory()) {
          EncryptFolder(file, password);
        } else if (!file.getPath().endsWith(".enc")) {
          EncryptFile(file, new File(file.getPath() + ".enc"), password);
        }
      }
      return true;
    } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException e) {
      ShowErr(e.getMessage());
    }
    return false;
  }

  private boolean DecryptFile(File sourceFile, File decryptFile, String password) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
    if (!sourceFile.getPath().endsWith(".enc")) {
      ShowErr("该文件未被加密！");
      return false;
    }
    InputStream inputStream = null;
    OutputStream outputStream = null;
    try {
      Cipher cipher = initAESCipher(password, Cipher.DECRYPT_MODE);
      inputStream = new FileInputStream(sourceFile);
      outputStream = new FileOutputStream(decryptFile);
      CipherOutputStream cipherOutputStream = new CipherOutputStream(
        outputStream, cipher);
      byte[] buffer = new byte[1024];
      int r;
      while ((r = inputStream.read(buffer)) >= 0) {
        cipherOutputStream.write(buffer, 0, r);
      }
      cipherOutputStream.close();
      sourceFile.delete();
      return true;
    } catch (IOException | InvalidKeyException e) {
      e.printStackTrace();
      ShowErr(e.getMessage());
    } finally {
      try {
        inputStream.close();
      } catch (IOException e) {
        e.printStackTrace(); // To change body of catch statement use
        // File | Settings | File Templates.
      }
      try {
        outputStream.close();
      } catch (IOException e) {
        e.printStackTrace(); // To change body of catch statement use
        // File | Settings | File Templates.
      }
    }
    return false;
  }

  private boolean DecryptFolder(File folder, String password) {
    File[] fileList = folder.listFiles();
    try {
      for (File file : Objects.requireNonNull(fileList)) {
        if (file.isDirectory()) {
          DecryptFolder(file, password);
        } else if (file.getPath().endsWith(".enc")) {
          DecryptFile(file, new File(file.getPath().replace(".enc", "")), password);
        }
      }
      return true;
    } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException e) {
      ShowErr(e.getMessage());
    }
    return false;
  }

  private void ShowErr(String bodyMsg) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("错误");
    alert.setHeaderText("发生错误");
    alert.setContentText(bodyMsg);

    alert.showAndWait();
  }

  private void ShowInfo(String info) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("信息");
    alert.setHeaderText(null);
    alert.setContentText(info);

    alert.showAndWait();
  }
}