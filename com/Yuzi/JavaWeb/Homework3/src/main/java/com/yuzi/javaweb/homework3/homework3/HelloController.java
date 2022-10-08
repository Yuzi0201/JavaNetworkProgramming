package com.yuzi.javaweb.homework3.homework3;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.*;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class HelloController {

  public RadioButton radioFile;
  public RadioButton radioFolder;
  public Button buttonChooseObject;
  public Label path;
  public VBox root;
  ToggleGroup groupObjectType = new ToggleGroup();

  @FXML
  protected void initialize() {
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

  public void onObjectButtonClick() {
    if (groupObjectType.getSelectedToggle() == radioFile) {
      FileChooser fileChooser = new FileChooser();
      fileChooser.setTitle("选择要操作的文件");
      File selectedFile = fileChooser.showOpenDialog(root.getScene().getWindow());
      path.setText("路径：" + selectedFile.getPath());
    } else {
      DirectoryChooser directoryChooser = new DirectoryChooser();
      directoryChooser.setTitle("选择要操作的文件夹");
      File selectedFolder = directoryChooser.showDialog(root.getScene().getWindow());
      path.setText("路径：" + selectedFolder.getPath());
    }
  }

  public void onExecuteButtonClick() {
    if (Objects.equals(path.getText(), "路径：")) {
      ShowErr("路径为空！");
      return;
    }
    boolean res;
    if (groupObjectType.getSelectedToggle() == radioFile) {
      res = CompressFile(new File(path.getText().replaceFirst("路径：", "")));
    } else {
      res = CompressFolder(new File(path.getText().replaceFirst("路径：", "")));
    }
    if (res) {
      ShowInfo("压缩成功！");
    } else {
      ShowErr("压缩失败！");
    }
  }

  protected boolean CompressFile(File sourceFile) {
    FileInputStream fileInputStream = null;
    FileOutputStream fileOutputStream = null;
    ZipOutputStream zipOutputStream = null;
    try {
      File compresssedFile = new File(sourceFile.getPath() + ".zip");
      fileInputStream = new FileInputStream(sourceFile);
      fileOutputStream = new FileOutputStream(compresssedFile);
      zipOutputStream = new ZipOutputStream(fileOutputStream);
      ZipEntry zipEntry = new ZipEntry(sourceFile.getName());
      zipOutputStream.putNextEntry(zipEntry);
      byte[] bytes = new byte[1024];
      int length;
      while ((length = fileInputStream.read(bytes)) >= 0) {
        zipOutputStream.write(bytes, 0, length);
      }
      return true;
    } catch (IOException e) {
      ShowErr(e.getMessage());
    } finally {
      try {
        assert zipOutputStream != null;
        zipOutputStream.close();
        fileInputStream.close();
        fileOutputStream.close();
      } catch (IOException e) {
        //ShowErr(e.getMessage());
        e.printStackTrace();
      }
    }
    return false;
  }

  protected boolean CompressFolder(File sourceFile) {
    FileOutputStream fileOutputStream = null;
    ZipOutputStream zipOutputStream = null;
    try {
      fileOutputStream = new FileOutputStream(sourceFile.getPath() + ".zip");
      zipOutputStream = new ZipOutputStream(fileOutputStream);
      return CompressFolderFile(sourceFile, sourceFile.getName(), zipOutputStream);
    } catch (IOException e) {
      ShowErr(e.getMessage());
    } finally {
      try {
        assert zipOutputStream != null;
        zipOutputStream.close();
        fileOutputStream.close();
      } catch (IOException e) {
        ShowErr(e.getMessage());
      }
    }
    return false;
  }

  protected boolean CompressFolderFile(File sourceFile, String fileName, ZipOutputStream zipOutputStream) {
    try {
      if (sourceFile.isDirectory()) {
        zipOutputStream.putNextEntry(new ZipEntry(fileName + "/"));
        zipOutputStream.closeEntry();
        File[] files = sourceFile.listFiles();
        for (File file : Objects.requireNonNull(files)) {
          CompressFolderFile(file, fileName + "/" + file.getName(), zipOutputStream);
        }
      } else {
        FileInputStream fileInputStream = new FileInputStream(sourceFile);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOutputStream.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fileInputStream.read(bytes)) >= 0) {
          zipOutputStream.write(bytes, 0, length);
        }
        fileInputStream.close();
      }
      return true;
    } catch (IOException e) {
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