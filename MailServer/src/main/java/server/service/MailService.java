package server.service;

import common.Email;
import server.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MailService {

    // 1. Hàm gửi Email (Đã nâng cấp thêm AI và Folder)
    public String sendEmail(Email email) {
        // --- BƯỚC 1: GỌI AI KIỂM TRA SPAM ---
        // Nếu chưa có file AISpamDetector, hãy tạo nó như hướng dẫn trước
        boolean isSpam = AISpamDetector.checkSpam(email.getSubject(), email.getBody());
        String folder = isSpam ? "SPAM" : "INBOX";

        // --- BƯỚC 2: LƯU VÀO DB ---
        // Câu lệnh SQL có thêm cột 'folder'
        String sql = "INSERT INTO emails(sender, receiver, subject, body, folder) VALUES(?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email.getSender());
            pstmt.setString(2, email.getReceiver());
            pstmt.setString(3, email.getSubject());
            pstmt.setString(4, email.getBody());
            pstmt.setString(5, folder); // Lưu trạng thái Inbox hay Spam

            pstmt.executeUpdate();

            // Trả về kết quả để Server báo lại cho Client
            return isSpam ? "SPAM_DETECTED" : "OK";

        } catch (SQLException e) {
            e.printStackTrace();
            if ("23503".equals(e.getSQLState())) {
                return "USER_NOT_FOUND"; // Người nhận không tồn tại
            }
            return "ERROR: " + e.getMessage();
        }

    }

    // 2. Hàm lấy danh sách Email (Nâng cấp để lọc theo Folder và lấy cả ID)
    // Tham số folderName: Client sẽ gửi lên là "INBOX" hoặc "SPAM"
    public List<Email> getEmails(String username, String folderType) {
        List<Email> listEmail = new ArrayList<>();
        String sql;

        // --- LOGIC QUAN TRỌNG Ở ĐÂY ---
        if ("SENT".equals(folderType)) {
            // Nếu xem thư đã gửi -> Tìm theo người gửi (SENDER)
            sql = "SELECT * FROM emails WHERE sender = ? ORDER BY created_at DESC";
        } else {
            // Nếu xem Inbox hoặc Spam -> Tìm theo người nhận (RECEIVER) và loại thư mục
            sql = "SELECT * FROM emails WHERE receiver = ? AND folder = ? ORDER BY created_at DESC";
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            // Nếu không phải là SENT thì mới cần set tham số thứ 2 (folder)
            if (!"SENT".equals(folderType)) {
                pstmt.setString(2, folderType);
            }

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Email e = new Email();
                e.setId(rs.getInt("id"));
                e.setSender(rs.getString("sender"));
                e.setReceiver(rs.getString("receiver")); // Lấy thêm cột này để hiển thị
                e.setSubject(rs.getString("subject"));
                e.setBody(rs.getString("body"));
                e.setFolder(rs.getString("folder"));
                e.setCreatedAt(rs.getTimestamp("created_at"));

                listEmail.add(e);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listEmail;
    }

    // 3. Hàm Gỡ Spam (Chuyển từ SPAM sang INBOX)
    public boolean markNotSpam(int emailId) {
        String sql = "UPDATE emails SET folder = 'INBOX' WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, emailId);
            return pstmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Thêm vào cuối file MailService.java
    public boolean deleteEmail(int id) {
        String sql = "DELETE FROM emails WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}