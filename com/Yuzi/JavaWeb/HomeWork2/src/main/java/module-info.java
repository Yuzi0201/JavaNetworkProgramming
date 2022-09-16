module com.yuzi.javaweb.homework2 {
  requires javafx.controls;
  requires javafx.fxml;


  opens com.yuzi.javaweb.homework2 to javafx.fxml;
  exports com.yuzi.javaweb.homework2;
}