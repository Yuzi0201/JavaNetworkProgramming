package com.yuzi.javaweb.homework4;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.Thread.sleep;

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

  public boolean flag = true;

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
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Encrypted file", "*.enc"));
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
  protected void onExecuteButtonClick() throws InterruptedException {
    String objectPath = path.getText().replaceFirst("路径：", "");
    if (objectPath.equals("")) {
      ShowErr("没有选择文件或文件夹！");
      return;
    }
    String password = passWordField.getText();
    flag = true;
    if (groupCryptType.getSelectedToggle() == radioEncrypt) {
      if (groupObjectType.getSelectedToggle() == radioFile) {
//        res = EncryptFile(new File(objectPath), new File(objectPath + ".enc"), password);
        Thread thread = new EncryptFile(new File(objectPath), new File(objectPath + ".enc"), password);
        thread.start();
        thread.join();
      } else {//对于文件夹，使用Executors.newCachedThreadPool()创建可变长线程池来进行多线程并发。
        ExecutorService pool = Executors.newCachedThreadPool();
        EncryptFolder(new File(objectPath), password, pool);
        pool.shutdown();
        while (!pool.isTerminated()) {
          sleep(100);
        }
      }
      if (flag) {
        ShowInfo("加密成功！");
      } else {
        ShowErr("加密失败！");
      }
    } else {
      if (groupObjectType.getSelectedToggle() == radioFile) {
        //DecryptFile(new File(objectPath), new File(objectPath.replace(".enc", "")), password);
        Thread thread = new DecryptFile(new File(objectPath), new File(objectPath.replace(".enc", "")), password);
        thread.start();
        thread.join();
      } else {//对于文件夹，使用Executors.newCachedThreadPool()创建可变长线程池来进行多线程并发。
        ExecutorService pool = Executors.newCachedThreadPool();
        DecryptFolder(new File(objectPath), password, pool);
        pool.shutdown();
        while (!pool.isTerminated()) {
          sleep(100);
        }
      }
      if (flag) {
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

/*
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
*/

  class EncryptFile extends Thread {
    File sourceFile;
    File encryptFile;
    String password;

    public EncryptFile(File sourceFile, File encryptFile, String password) {
      this.sourceFile = sourceFile;
      this.encryptFile = encryptFile;
      this.password = password;
    }

    @Override
    public void run() {
      if (sourceFile.getPath().endsWith(".enc")) {
        ShowErr("该文件已被加密过！");
        //return false;
        flag = false;
        return;
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
        return;
      } catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
        e.printStackTrace();
        ShowErr(e.getMessage());
      } finally {
        try {
          assert inputStream != null;
          inputStream.close();
        } catch (IOException e) {
          e.printStackTrace();
          ShowErr(e.getMessage());
        }
        try {
          assert outputStream != null;
          outputStream.close();
        } catch (IOException e) {
          e.printStackTrace();
          ShowErr(e.getMessage());
        }
      }
      //return false;
      flag = false;
    }
  }

  private void EncryptFolder(File folder, String password, ExecutorService pool) {
    File[] fileList = folder.listFiles();
    for (File file : Objects.requireNonNull(fileList)) {
      if (file.isDirectory()) {
        EncryptFolder(file, password, pool);
      } else if (!file.getPath().endsWith(".enc")) {
        //EncryptFile(file, new File(file.getPath() + ".enc"), password);
        Thread thread = new EncryptFile(file, new File(file.getPath() + ".enc"), password);
        //thread.start();
        pool.execute(thread);
      }
    }
  }

/*
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
        e.printStackTrace();
      }
      try {
        outputStream.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return false;
  }
*/

  class DecryptFile extends Thread {
    File sourceFile;
    File decryptFile;
    String password;

    public DecryptFile(File sourceFile, File decryptFile, String password) {
      this.sourceFile = sourceFile;
      this.decryptFile = decryptFile;
      this.password = password;
    }

    @Override
    public void run() {
      if (!sourceFile.getPath().endsWith(".enc")) {
        ShowErr("该文件未被加密！");
        //return false;
        flag = false;
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
        return;
      } catch (IOException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
        e.printStackTrace();
        ShowErr(e.getMessage());
      } finally {
        try {
          inputStream.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
        try {
          outputStream.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      //return false;
      flag = false;
    }
  }

  private void DecryptFolder(File folder, String password, ExecutorService pool) {
    File[] fileList = folder.listFiles();
    for (File file : Objects.requireNonNull(fileList)) {
      if (file.isDirectory()) {
        DecryptFolder(file, password, pool);
      } else if (file.getPath().endsWith(".enc")) {
        //DecryptFile(file, new File(file.getPath().replace(".enc", "")), password);
        Thread thread = new DecryptFile(file, new File(file.getPath().replace(".enc", "")), password);
        //thread.start();
        pool.execute(thread);
      }
    }
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