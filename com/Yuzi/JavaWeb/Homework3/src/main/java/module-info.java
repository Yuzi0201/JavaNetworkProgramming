module com.yuzi.javaweb.homework3.homework3 {
  requires javafx.controls;
  requires javafx.fxml;


  opens com.yuzi.javaweb.homework3.homework3 to javafx.fxml;
  exports com.yuzi.javaweb.homework3.homework3;
}