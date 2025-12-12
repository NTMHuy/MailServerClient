package client.controller;

import client.SocketClient;
import client.voice.AudioCapture;
import common.Email;
import common.Request;
import common.Response;
import common.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import client.voice.VoskService;
import client.voice.VoiceInputListener;


public class ComposeController {
    @FXML private TextField txtTo, txtSubject;
    @FXML private TextArea txtBody;
    @FXML private Label lblStatus;

    private User currentUser;
    private SocketClient client = new SocketClient();

    private boolean micOn = false;
    private AudioCapture capture;

    private VoskService vosk;

    public void setup(User user) {

        this.currentUser = user;

        vosk = new VoskService(new VoiceInputListener() {

//            @Override
//            public void onStatus(String status) {
//                Platform.runLater(() -> lblStatus.setText(status));
//            }
//
//            @Override
//            public void onPartial(String text) {
//                Platform.runLater(() -> lblStatus.setText("🎤 " + text));
//            }
//
//            @Override
//            public void onFinal(String text) {
//                Platform.runLater(() -> {
//                    // Chèn kết quả giọng nói vào vị trí con trỏ
//                    txtBody.insertText(txtBody.getCaretPosition(), " " + text + " ");
//                    lblStatus.setText("");  // clear trạng thái
//                });
//            }
//
//            @Override
//            public void onError(Exception e) {
//
//            }

            @Override
            public void onStatus(String status) {
                System.out.println("Controller.onStatus: " + status);
                Platform.runLater(() -> lblStatus.setText(status));
            }

            @Override
            public void onPartial(String text) {
                System.out.println("Controller.onPartial: [" + text + "]");
                Platform.runLater(() -> lblStatus.setText("🎤 " + text));
            }

            @Override
            public void onFinal(String text) {
                System.out.println("Controller.onFinal: [" + text + "]");
                Platform.runLater(() -> {
                    txtBody.insertText(txtBody.getCaretPosition(), " " + text + " ");
                    lblStatus.setText("");
                });
            }

            @Override
            public void onError(Exception e) {
                System.err.println("Controller.onError:");
                e.printStackTrace();
                Platform.runLater(() -> lblStatus.setText("❌ Lỗi: " + e.getMessage()));
            }
        });
    }

    public void setPreFilledData(String to, String subject, String body) {
        if (to != null) {
            txtTo.setText(to);
            txtTo.setDisable(true); // Có thể khóa lại nếu muốn bắt buộc gửi cho người đó
        }

        if (subject != null) {
            txtSubject.setText(subject);
        }

        if (body != null) {
            txtBody.setText("\n\n" + body);
            Platform.runLater(() -> txtBody.positionCaret(0));
        }
    }

    @FXML
    public void handleVoice() {
        if (!micOn) {
            micOn = true;
            Platform.runLater(() -> lblStatus.setText("🔄 Đang tải model…"));

            new Thread(() -> {
                try {
                    // Cứ loadModel, nếu model đã load thì VoskService sẽ bỏ qua (bạn đã code như vậy)
                    vosk.loadModel("D:\\tai_lieu_k5\\lap_trinh_mang\\MailClient\\src\\main\\resources\\models\\vosk-model-vn-0.4");

                    capture = new AudioCapture();
                    vosk.startRecognition(capture);

                    Platform.runLater(() -> lblStatus.setText("🎤 Đang nghe… (bấm lại để tắt)"));

                } catch (Exception e) {
                    e.printStackTrace();
                    micOn = false;
                    Platform.runLater(() -> lblStatus.setText("❌ Lỗi microphone!"));
                }
            }).start();

        } else {
            micOn = false;

            try {
                vosk.stopRecognition();
            } catch (Exception ignored) {}

            try {
                if (capture != null) {
                    capture.stop();
                }
            } catch (Exception ignored) {}

            Platform.runLater(() -> lblStatus.setText("⏹ Đã tắt microphone."));
        }
    }


    @FXML
    public void handleSend() {
        if (txtTo.getText().trim().isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập người nhận!");
            return;
        }
        if (txtSubject.getText().trim().isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập tiêu đề!");
            return;
        }

        Email email = new Email(
                currentUser.getUsername(),
                txtTo.getText(),
                txtSubject.getText(),
                txtBody.getText()
        );

        Response res = client.sendRequest(new Request("SEND", email));

        showAlert("Thông báo", res.message);

        if (res.success) {
            // Đóng cửa sổ soạn thảo
            ((Stage) txtTo.getScene().getWindow()).close();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}