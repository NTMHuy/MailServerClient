package client.controller;

import client.RMIClient;
import client.SocketClient;
import common.Request;
import common.Response;
import common.SecurityUtils;
import common.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class RegisterController {
    @FXML private TextField txtName, txtUser;
    @FXML private PasswordField txtPass, txtConfirmPass;

    private SocketClient client = new SocketClient();
    private RMIClient rmiClient = new RMIClient();

    @FXML
    public void handleRegister() {
        String name = txtName.getText().trim();
        String user = txtUser.getText().trim();
        String pass = txtPass.getText();
        String confirm = txtConfirmPass.getText();

        if (name.isEmpty() || user.isEmpty() || pass.isEmpty()) {
            showAlert("Lỗi", "Vui lòng điền đầy đủ thông tin!");
            return;
        }

        if (!pass.equals(confirm)) {
            showAlert("Lỗi", "Mật khẩu xác nhận không khớp!");
            return;
        }

        if (pass.length() < 6) {
            showAlert("Lỗi", "Mật khẩu phải có ít nhất 6 ký tự!");
            return;
        }

        String encryptedPass = SecurityUtils.encrypt(pass);

        if (encryptedPass == null) {
            showAlert("Lỗi hệ thống", "Không thể mã hóa mật khẩu.");
            return;
        }

        User newUser = new User(user, encryptedPass, name);
//        Response res = client.sendRequest(new Request("REGISTER", newUser));
//
//        if (res.success) {
//            showAlert("Thành công", "Tạo tài khoản thành công! Vui lòng đăng nhập.");
//            handleBackToLogin(); // Chuyển về màn hình đăng nhập
//        } else {
//            showAlert("Thất bại", res.message); // Ví dụ: Username đã tồn tại
//        }

        //rmi register
        try {
            boolean success = rmiClient.register(newUser);

            if (success) {
                showAlert("Thành công", "Tạo tài khoản thành công! Vui lòng đăng nhập.");
                handleBackToLogin();
            } else {
                showAlert("Thất bại", "Tên đăng nhập đã tồn tại!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi mạng", "Không kết nối được đến RMI Server.");
        }
    }

    @FXML
    public void handleBackToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Stage stage = (Stage) txtUser.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}