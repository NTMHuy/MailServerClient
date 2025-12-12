package client.controller;

import client.SocketClient;
import common.Email;
import common.Request;
import common.Response;
import common.User;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;

public class DashboardController {
    @FXML private Label lblUser, lblFolderTitle;
    @FXML private TableView<Email> tableEmail;
    @FXML private TableColumn<?, ?> colId, colSender, colSubject, colTime;

    private User currentUser;
    private SocketClient client = new SocketClient();

    // --- BIẾN ĐỂ TỰ ĐỘNG LÀM MỚI ---
    private Timeline autoRefreshTimer;
    private String currentFolder = "INBOX"; // Lưu trạng thái đang xem folder nào

    public String getCurrentFolder() {
        return this.currentFolder;
    }

    public void initData(User user) {
        this.currentUser = user;
        lblUser.setText(user.getFullName());
        loadInbox(); // Mặc định vào là load Inbox ngay

        // BẮT ĐẦU CHẠY ĐỒNG HỒ TỰ ĐỘNG CẬP NHẬT
        startAutoRefresh();
    }

    @FXML
    public void initialize() {
        // Cấu hình cột
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colSender.setCellValueFactory(new PropertyValueFactory<>("sender"));
        colSubject.setCellValueFactory(new PropertyValueFactory<>("subject"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        // Sự kiện click đúp
        tableEmail.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && tableEmail.getSelectionModel().getSelectedItem() != null) {
                // Tạm dừng update khi đang đọc thư để tránh giật lag
                pauseAutoRefresh();
                openDetail(tableEmail.getSelectionModel().getSelectedItem());
            }
        });
    }

    // --- LOGIC TỰ ĐỘNG CẬP NHẬT ---
    private void startAutoRefresh() {
        // Cứ 5 giây (Duration.seconds(5)) sẽ chạy đoạn code bên trong 1 lần
        autoRefreshTimer = new Timeline(new KeyFrame(Duration.seconds(5), event -> {
            // Chỉ update ngầm, không in log để đỡ rối
            refreshTableSilent();
        }));
        autoRefreshTimer.setCycleCount(Timeline.INDEFINITE); // Chạy mãi mãi
        autoRefreshTimer.play(); // Bắt đầu
    }

    private void pauseAutoRefresh() {
        if(autoRefreshTimer != null) autoRefreshTimer.pause();
    }

    public void resumeAutoRefresh() {
        if(autoRefreshTimer != null) autoRefreshTimer.play();
    }

    private void stopAutoRefresh() {
        if(autoRefreshTimer != null) autoRefreshTimer.stop();
    }

    // --- CÁC HÀM LOAD DỮ LIỆU ---

    @FXML
    public void loadInbox() {
        currentFolder = "INBOX";
        lblFolderTitle.setText("Hộp thư đến");
        colSender.setText("Người gửi");
        colSender.setCellValueFactory(new PropertyValueFactory<>("sender"));
        loadEmails("INBOX");
    }

    @FXML
    public void loadSpam() {
        currentFolder = "SPAM";
        lblFolderTitle.setText("Thư rác (Spam)");
        colSender.setText("Người gửi");
        colSender.setCellValueFactory(new PropertyValueFactory<>("sender"));
        loadEmails("SPAM");
    }

    @FXML
    public void loadSent() {
        currentFolder = "SENT";
        lblFolderTitle.setText("Thư đã gửi");
        colSender.setText("Gửi đến");
        colSender.setCellValueFactory(new PropertyValueFactory<>("receiver"));
        loadEmails("SENT");
    }

    @FXML
    public void loadTrash() {
        currentFolder = "TRASH";
        lblFolderTitle.setText("Thùng rác");
        colSender.setText("Người gửi");
        colSender.setCellValueFactory(new PropertyValueFactory<>("sender"));
        loadEmails("TRASH");
    }

    // Hàm load chính thức (Có hiển thị loading nếu cần)
    private void loadEmails(String folder) {
        Response res = client.sendRequest(new Request("GET_EMAILS", new String[]{currentUser.getUsername(), folder}));
        if (res.success) {
            updateTableData((List<Email>) res.data);
        }
    }

    // Hàm load ngầm (Chạy bởi Timer)
    private void refreshTableSilent() {
        // Gửi request lấy dữ liệu mới nhất của folder đang xem
        Response res = client.sendRequest(new Request("GET_EMAILS", new String[]{currentUser.getUsername(), currentFolder}));
        if (res.success) {
            List<Email> newData = (List<Email>) res.data;
            // So sánh kích thước hoặc ID mới nhất để xem có tin mới không, nếu thích
            // Ở đây ta cứ cập nhật lại bảng luôn
            updateTableData(newData);
        }
    }

    // Hàm cập nhật bảng chung
    private void updateTableData(List<Email> list) {
        // Lưu lại dòng đang chọn (nếu có) để sau khi refresh không bị mất chọn
        Email selected = tableEmail.getSelectionModel().getSelectedItem();
        int selectedIndex = tableEmail.getSelectionModel().getSelectedIndex();

        tableEmail.setItems(FXCollections.observableArrayList(list));

        // Khôi phục lại dòng đang chọn (nếu nó vẫn còn trong list)
        if (selected != null && selectedIndex >= 0 && selectedIndex < list.size()) {
            tableEmail.getSelectionModel().select(selectedIndex);
        }
    }

    // --- CÁC HÀM KHÁC ---

    @FXML
    public void handleCompose() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/compose.fxml"));
            Parent root = loader.load();
            ComposeController ctrl = loader.getController();
            ctrl.setup(currentUser);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Soạn thư mới");
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void openDetail(Email email) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/detail.fxml"));
            Parent root = loader.load();

            DetailController ctrl = loader.getController();
            ctrl.setup(email, this, currentUser);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle(email.getSubject());

            // Khi đóng cửa sổ đọc thư thì CHẠY LẠI AUTO REFRESH
            stage.setOnHidden(e -> resumeAutoRefresh());

            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // Hàm được gọi từ bên ngoài để refresh thủ công
    public void refreshTable() {
        refreshTableSilent();
    }

    @FXML
    public void handleLogout() {
        stopAutoRefresh(); // Dừng đồng hồ khi thoát
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Stage stage = (Stage) lblUser.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) { e.printStackTrace(); }
    }
}