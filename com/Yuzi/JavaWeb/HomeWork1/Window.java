package com.Yuzi.JavaWeb.HomeWork1;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

class Window extends JFrame {
  JTextField number;
  JButton button;

  public Window() {
    init();
    setResizable(false);
    setVisible(true);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }

  private void init() {
    setLayout(null);
    JLabel jLabel = new JLabel("请输入一个十进制小数");
    jLabel.setBounds(90, 20, 300, 80);
    jLabel.setFont(new Font("Dialog", Font.PLAIN, 25));
    add(jLabel);
    number = new JTextField(10);
    number.setBounds(130, 90, 150, 40);
    add(number);
    button = new JButton("确定");
    button.setBounds(170, 150, 70, 30);
    add(button);
    button.addActionListener(e -> Caculator());
    number.addKeyListener(new KeyListener() {
      public void keyTyped(KeyEvent keyEvent) {
      }

      public void keyPressed(KeyEvent keyEvent) {
        if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER)
          Caculator();
      }

      public void keyReleased(KeyEvent keyEvent) {
      }
    });

  }

  void Caculator() {
    String result;
    String text = number.getText();
    if (text.endsWith(".")) {
      ShowErrorDialog("这不是一个合法的十进制小数！");
      return;
    }
    String[] splitBuffer = number.getText().split("\\.");
    if (splitBuffer.length == 2) {
      try {
        int flag = 0;//0为整数部分，1为小数部分
        for (String s : splitBuffer) {
          for (int i = s.length() - 1; i >= 0; i--) {
            if (!Character.isDigit(s.charAt(i))) {
              throw new Exception("这不是一个合法的十进制小数！");
            }
          }
          if (s.startsWith("0") || s.endsWith("0"))
            throw new Exception("这不是一个合法的十进制小数！");

          splitBuffer[flag] = new StringBuffer(s).reverse().toString();
          flag++;
        }
        result = splitBuffer[0] + "." + splitBuffer[1];
        ShowDialog("整数小数分别翻转后的结果是：\n" + result);
      } catch (Exception exception) {
        ShowErrorDialog(exception.getMessage());
      }
    } else {
      ShowErrorDialog("这不是一个合法的十进制小数！");
    }
  }

  void ShowErrorDialog(String Message) {
    JOptionPane.showMessageDialog(null, Message, "错误", JOptionPane.ERROR_MESSAGE);
  }

  void ShowDialog(String Message) {
    JOptionPane.showMessageDialog(null, Message, "结果", JOptionPane.INFORMATION_MESSAGE);
  }
}
