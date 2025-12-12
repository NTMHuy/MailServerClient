package client.controller;

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

public class LoginController {
    @FXML private TextField txtUser;
    @FXML private PasswordField txtPass;

    // Kết nối Socket
    private SocketClient client = new SocketClient();

    @FXML
    public void handleLogin() {
        String user = txtUser.getText().trim();
        String rawPass = txtPass.getText();

        // 1. Kiểm tra nhập liệu
        if (user.isEmpty() || rawPass.isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu!", Alert.AlertType.WARNING);
            return;
        }


        String encryptedPass = SecurityUtils.encrypt(rawPass);

        if (encryptedPass == null) {
            showAlert("Lỗi hệ thống", "Không thể mã hóa mật khẩu.", Alert.AlertType.ERROR);
            return;
        }

        try {
            Response res = client.sendRequest(new Request("LOGIN", new User(user, encryptedPass, null)));

            if (res.success) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/dashboard.fxml"));
                Parent root = loader.load();

                DashboardController ctrl = loader.getController();
                ctrl.initData((User) res.data); // Truyền thông tin user sang Dashboard

                Stage stage = (Stage) txtUser.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("TH Mail - Hộp thư đến");
                stage.centerOnScreen();
            } else {
                showAlert("Đăng nhập thất bại", res.message, Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi mạng", "Không kết nối được đến Server. Hãy kiểm tra lại đường truyền.", Alert.AlertType.ERROR);
        }
    }

    // --- XỬ LÝ CHUYỂN SANG MÀN HÌNH ĐĂNG KÝ ---
    @FXML
    public void handleRegister() {
        try {
            // Tải file giao diện đăng ký
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/register.fxml"));
            Parent root = loader.load();

            // Lấy Stage hiện tại và thay đổi Scene
            Stage stage = (Stage) txtUser.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("TH Mail - Đăng ký tài khoản mới");
            stage.centerOnScreen();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể tải màn hình đăng ký: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // Hàm hiển thị thông báo chung
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}