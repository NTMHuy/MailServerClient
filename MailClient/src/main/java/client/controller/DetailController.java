package client.controller;

import client.SocketClient;
import client.ai.SummarizerClient;
import common.Email;
import common.Request;
import common.Response;
import common.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class DetailController {
    @FXML private Label lblSubject, lblSender, lblTime, lblAiStatus;
    @FXML private TextArea txtContent, txtSummary;

    // Nhóm nút hành động
    @FXML private HBox boxSpamActions;
    @FXML private Button btnActionMain; // Nút xóa (sẽ đổi chữ tùy ngữ cảnh)
    @FXML private Button btnRestore;    // Nút khôi phục (chỉ hiện trong thùng rác)

    private Email email;
    private DashboardController dashboardController;
    private User currentUser; // User hiện tại (Cần để biết ai đang Reply)
    private SocketClient client = new SocketClient();

    // --- HÀM SETUP (CẬP NHẬT: NHẬN 3 THAM SỐ) ---
    public void setup(Email email, DashboardController dashboard, User user) {
        this.email = email;
        this.dashboardController = dashboard;
        this.currentUser = user;

        // 1. Hiển thị thông tin
        lblSubject.setText(email.getSubject());
        lblSender.setText(email.getSender());
        lblTime.setText(email.getCreatedAt().toString());
        txtContent.setText(email.getBody());

        // 2. Xử lý logic hiển thị nút bấm
        boxSpamActions.setVisible(true); // Luôn hiện thanh công cụ
        if (btnRestore != null) btnRestore.setVisible(false); // Mặc định ẩn nút khôi phục

        // Kiểm tra xem đang ở folder nào để hiển thị nút phù hợp
        String currentFolder = email.getFolder();

        if ("TRASH".equals(currentFolder)) {
            // Ở thùng rác -> Cho phép Khôi phục & Xóa vĩnh viễn
            btnActionMain.setText("🗑️ Xóa vĩnh viễn");
            btnActionMain.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white;");
            if (btnRestore != null) btnRestore.setVisible(true);

        } else if ("SENT".equals(dashboard.getCurrentFolder())) {
            // Ở thư đã gửi -> Xóa vĩnh viễn
            btnActionMain.setText("🗑️ Xóa");
            btnActionMain.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

        } else {
            // Ở Inbox/Spam -> Chuyển vào thùng rác (Xóa mềm)
            btnActionMain.setText("🗑️ Chuyển vào thùng rác");
            btnActionMain.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white;");
        }
    }

    // --- 1. XỬ LÝ TRẢ LỜI (REPLY) ---
    @FXML
    public void handleReply() {
        String originalSender = email.getSender();
        String newSubject = "Re: " + email.getSubject();
        // Trích dẫn nội dung cũ
        String quotedBody = "\n\n--- Vào lúc " + email.getCreatedAt() + ", " + originalSender + " đã viết: ---\n" + email.getBody();

        openComposeWindow(originalSender, newSubject, quotedBody);
    }

    // --- 2. XỬ LÝ CHUYỂN TIẾP (FORWARD) ---
    @FXML
    public void handleForward() {
        String newSubject = "Fwd: " + email.getSubject();
        String quotedBody = "\n\n--- Thư chuyển tiếp từ " + email.getSender() + " ---\n" + email.getBody();

        // Để trống người nhận ("")
        openComposeWindow("", newSubject, quotedBody);
    }

    // Hàm phụ mở cửa sổ soạn thảo
    private void openComposeWindow(String to, String sub, String body) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/compose.fxml"));
            Parent root = loader.load();

            ComposeController ctrl = loader.getController();
            ctrl.setup(currentUser); // Truyền user hiện tại
            ctrl.setPreFilledData(to, sub, body); // Điền dữ liệu

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle(sub);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi: Không mở được cửa sổ soạn thảo.");
        }
    }

    // --- 3. XỬ LÝ XÓA (DELETE) ---
    @FXML
    public void handleDelete() {
        String currentFolder = email.getFolder();

        // Nếu đang ở Thùng rác hoặc Thư đã gửi -> Xóa vĩnh viễn
        if ("TRASH".equals(currentFolder) || "SENT".equals(currentFolder)) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Hành động này không thể hoàn tác. Bạn chắc chắn muốn xóa vĩnh viễn?", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait();

            if (confirm.getResult() == ButtonType.YES) {
                Response res = client.sendRequest(new Request("DELETE_MAIL", email.getId()));
                showAlertAndClose(res.message);
            }
        } else {
            // Nếu ở Inbox/Spam -> Chuyển vào thùng rác (Soft Delete)
            Response res = client.sendRequest(new Request("MOVE_TO_TRASH", email.getId()));
            showAlertAndClose(res.message);
        }
    }

    // --- 4. XỬ LÝ KHÔI PHỤC (RESTORE) ---
    @FXML
    public void handleRestore() {
        // Gọi lệnh UNSPAM (Bản chất là set folder = INBOX)
        Response res = client.sendRequest(new Request("UNSPAM", email.getId()));
        showAlertAndClose("Đã khôi phục thư về Inbox!");
    }

    @FXML
    public void handleUnspam() {
        handleRestore(); // Dùng chung logic khôi phục
    }

    // --- 5. AI TÓM TẮT ---
    @FXML
    public void handleSummarize() {
        lblAiStatus.setText("🤖 AI đang đọc...");
        txtSummary.clear();

        new Thread(() -> {
            String result = SummarizerClient.summarize(email.getBody());

            Platform.runLater(() -> {
                txtSummary.setText(result);
                lblAiStatus.setText("✅ Hoàn tất!");
            });
        }).start();
    }

    private String summarizeLogic(String text) {
        if (text == null) return "";
        if (text.length() < 100) return text;
        return text.substring(0, 100) + "... (Và nhiều nội dung khác)";
    }

    // Hàm tiện ích: Hiện thông báo, đóng cửa sổ và refresh bảng
    private void showAlertAndClose(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg);
        alert.showAndWait();

        if (dashboardController != null) dashboardController.refreshTable();

        ((Stage) lblSubject.getScene().getWindow()).close();
    }

    private void showAlert(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg).showAndWait();
    }
}