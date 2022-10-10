module com.yuzi.javaweb.homework4 {
  requires javafx.controls;
  requires javafx.fxml;


  opens com.yuzi.javaweb.homework4 to javafx.fxml;
  exports com.yuzi.javaweb.homework4;
}