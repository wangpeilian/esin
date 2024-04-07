package com.esin.base.encrypt;

import com.esin.base.utility.Logger;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * <p>acsii码加密共通类</p><br>
 */
public class CipherUtil extends JFrame implements ActionListener {
    private static final Logger logger = Logger.getLogger(CipherUtil.class);

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
        } catch (InstantiationException e) {
        } catch (IllegalAccessException e) {
        } catch (UnsupportedLookAndFeelException e) {
        }
        new CipherUtil();
    }

    private static String INPUT = "输入";
    private static String OUTPUT = "输出";
    private static String ENCODER = "加密";
    private static String DECODER = "解密";
    private static String LICENCE = "注册码";
    private JTextField input = null;
    private JTextField output = null;

    public CipherUtil() {
        input = new JTextField();
        output = new JTextField();
        setTitle("注册码生成器");
        JPanel topWindow = new JPanel();
        Box ContainerPane = Box.createVerticalBox();
        topWindow.add(ContainerPane);
        ContainerPane.add("Top", Box.createVerticalStrut(5));
        Box inputBox = Box.createHorizontalBox();
        inputBox.add("Left", new JLabel(INPUT + " : "));
        input.setColumns(100);
        inputBox.add("Right", input);
        ContainerPane.add("Top", inputBox);
        ContainerPane.add("Top", Box.createVerticalStrut(5));
        Box outputBox = Box.createHorizontalBox();
        outputBox.add("Left", new JLabel(OUTPUT + " : "));
        output.setColumns(100);
        outputBox.add("Right", output);
        ContainerPane.add(outputBox);
        Box btnBox = Box.createHorizontalBox();
        JButton btnEncoder = new JButton(ENCODER);
        btnEncoder.setActionCommand(ENCODER);
        btnEncoder.addActionListener(this);
        btnEncoder.setMnemonic('G');
        btnBox.add(btnEncoder);
        btnBox.add(Box.createHorizontalStrut(10));
        JButton btnDecoder = new JButton(DECODER);
        btnDecoder.setActionCommand(DECODER);
        btnDecoder.addActionListener(this);
        btnDecoder.setMnemonic('F');
        btnBox.add(btnDecoder);
        btnBox.add(Box.createHorizontalStrut(10));
        JButton btnLicence = new JButton(LICENCE);
        btnLicence.setActionCommand(LICENCE);
        btnLicence.addActionListener(this);
        btnLicence.setMnemonic('L');
        btnBox.add(btnLicence);
        ContainerPane.add("Bottom", Box.createVerticalStrut(5));
        ContainerPane.add("Bottom", btnBox);
        ContainerPane.add("Bottom", Box.createVerticalStrut(5));
        setBounds(300, 300, 800, 600);
        getContentPane().add(topWindow);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setVisible(true);
    }

    public void actionPerformed(ActionEvent event) {
        if (event.getActionCommand().equals(ENCODER)) {
            String key = input.getText();
            key = encrypt(key);
            if (key != null) {
                output.setText(key);
            } else {
                JOptionPane.showMessageDialog(this, "无法加密字符串");
            }
        } else if (event.getActionCommand().equals(DECODER)) {
            String key = input.getText();
            key = deciphering(key);
            if (key != null) {
                output.setText(key);
            } else {
                JOptionPane.showMessageDialog(this, "无法解密字符串");
            }
        } else if (event.getActionCommand().equals(LICENCE)) {
            String key = input.getText();
            key = deciphering(key);
            if (key != null) {
                key = new StringBuilder(key).reverse().toString();
                output.setText(encrypt(key));
            } else {
                JOptionPane.showMessageDialog(this, "无法生成注册码");
            }
        }
    }

    public static String encrypt(String str) {
        if (str == null || str.length() < 4) {
            return null;
        }
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c <= 32 || c >= 127) {
                return null;
            }
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < str.length(); i++) {
            double rand = Math.random();
            while (rand < 16 || rand > 36) {
                if (rand < 16) rand *= 7;
                if (rand > 36) rand /= 3;
            }
            int radix = (int) rand;
            String s = Integer.toString(str.charAt(i), radix);
            if (s.length() == 1) {
                sb.append("0");
            }
            sb.append(s);
            sb.append(Integer.toString(radix, 36));
            sb.insert(sb.length() - 3 + radix % 4, ' ');
        }
        int sum = 17;
        for (int i = 0; i < sb.length(); i++) {
            char c = sb.charAt(i);
            if (c == ' ') {
                c = (char) ('A' + sum % 26);
                sb.setCharAt(i, c);
            }
            sum += c;
        }
        return sb.reverse().toString();
    }

    public static String deciphering(String str) {
        if (str == null || str.length() == 0) {
            return null;
        }
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c < '0' || c > '9' && c < 'A' || c > 'Z' && c < 'a' || c > 'z') {
                return null;
            }
        }
        StringBuffer sb = new StringBuffer();
        int sum = 17;
        for (int i = str.length() - 1; i >= 0; i--) {
            char c = str.charAt(i);
            if (!Character.isUpperCase(c)) {
                sb.append(c);
            } else if (c != (char) ('A' + sum % 26)) {
                return null;
            }
            sum += c;
        }
        str = sb.toString();
        sb = new StringBuffer();
        try {
            for (int i = 0; i < str.length(); i += 3) {
                int radix = Integer.parseInt(str.substring(i + 2, i + 3), 36);
                sb.append((char) Integer.parseInt(str.substring(i, i + 2), radix));
            }
        } catch (Exception e) {
            logger.error("CipherUtil.deciphering 错误 : " + str, e);
            return null;
        }
        return sb.toString();
    }
}
