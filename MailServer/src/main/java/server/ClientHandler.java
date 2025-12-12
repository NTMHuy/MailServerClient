package server;

import common.Email;
import common.Request;
import common.Response;
import common.User;
import server.service.MailService;
import server.service.UserService;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {
    private Socket socket;
    private UserService userService;
    private MailService mailService; // Khai báo thêm MailService

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.userService = new UserService();
        this.mailService = new MailService(); // Khởi tạo service
    }

    @Override
    public void run() {
        try (
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())
        ) {
            while (true) {
                // 1. Đọc yêu cầu
                Request req = (Request) in.readObject();
                Response res = null;

                System.out.println("📩 Nhận yêu cầu: " + req.type);

                switch (req.type) {
                    // --- XỬ LÝ ĐĂNG NHẬP ---
                    case "LOGIN":
                        User uLog = (User) req.data;
                        User userResult = userService.login(uLog.getUsername(), uLog.getPassword());
                        if (userResult != null) {
                            res = new Response(true, "Đăng nhập thành công!", userResult);
                        } else {
                            res = new Response(false, "Sai tài khoản hoặc mật khẩu!", null);
                        }
                        break;

                    // --- XỬ LÝ ĐĂNG KÝ ---
                    case "REGISTER":
                        User uReg = (User) req.data;
                        boolean isReg = userService.register(uReg);
                        if (isReg) {
                            res = new Response(true, "Đăng ký thành công!", null);
                        } else {
                            res = new Response(false, "Tên đăng nhập đã tồn tại!", null);
                        }
                        break;

                    // --- XỬ LÝ GỬI THƯ (CẬP NHẬT) ---
                    case "SEND": // Client gửi lệnh là "SEND"
                        Email mailToSend = (Email) req.data;

                        // Gọi hàm gửi (đã có AI check bên trong)
                        String status = mailService.sendEmail(mailToSend);

                        if ("OK".equals(status)) {
                            res = new Response(true, "Gửi thư thành công!", null);
                        } else if ("SPAM_DETECTED".equals(status)) {
                            res = new Response(true, "Đã gửi (Cảnh báo: Thư bị AI chặn vào Spam)", null);
                        } else if ("USER_NOT_FOUND".equals(status)) {
                            res = new Response(false, "Gửi thất bại: Người nhận không tồn tại", null);
                        } else {
                            res = new Response(false, "Lỗi: " + status, null);
                        }
                        break;

                    // --- XỬ LÝ LẤY THƯ (INBOX HOẶC SPAM) ---
                    case "GET_EMAILS":
                        // Client gửi lên một mảng String: [username, folderName]
                        String[] params = (String[]) req.data;
                        String username = params[0];
                        String folder = params[1];

                        List<Email> emails = mailService.getEmails(username, folder);
                        res = new Response(true, "Lấy danh sách thành công", emails);
                        break;

                    // --- XỬ LÝ GỠ SPAM (MỚI) ---
                    case "UNSPAM":
                        int emailId = (Integer) req.data;
                        boolean unSpamSuccess = mailService.markNotSpam(emailId);

                        if (unSpamSuccess) {
                            res = new Response(true, "Đã chuyển thư về Hộp thư đến!", null);
                        } else {
                            res = new Response(false, "Lỗi: Không tìm thấy thư hoặc lỗi DB", null);
                        }
                        break;

                    case "EXIT":
                        System.out.println("Client ngắt kết nối.");
                        return;
                    // Thêm vào trong switch(req.type)

                    case "DELETE_MAIL":
                        int idDel = (Integer) req.data;
                        boolean isDeleted = mailService.deleteEmail(idDel); // Gọi hàm vừa tạo

                        if (isDeleted) {
                            res = new Response(true, "Đã xóa thư vĩnh viễn!", null);
                        } else {
                            res = new Response(false, "Lỗi khi xóa thư", null);
                        }
                        break;

                    default:
                        res = new Response(false, "Lệnh không hợp lệ: " + req.type, null);
                }

                // Gửi phản hồi
                out.writeObject(res);
                out.flush();
            }
        } catch (Exception e) {
            System.out.println("Client đã ngắt kết nối đột ngột.");
        }
    }
}