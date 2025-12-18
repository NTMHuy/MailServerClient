package server.service;

import common.SecurityUtils;
import common.User;
import server.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserService {


    public boolean register(User user) {
        // 1. Giải mã AES
        String rawPassword = SecurityUtils.decrypt(user.getPassword());

        if (rawPassword == null) {
            System.out.println("❌ Lỗi: Không thể giải mã mật khẩu AES từ Client gửi lên.");
            return false;
        }


        String hashedPassword = SecurityUtils.hashSHA256(rawPassword);


        String sql = "INSERT INTO users(username, password, full_name) VALUES(?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, hashedPassword); // Lưu chuỗi HASH (8d969e...)
            pstmt.setString(3, user.getFullName());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {

            return false;
        }
    }

    //XỬ LÝ ĐĂNG NHẬP

    public User login(String username, String aesPasswordFromClient) {
        String rawPassword = SecurityUtils.decrypt(aesPasswordFromClient);

        if (rawPassword == null) {
            System.out.println(" Lỗi: Giải mã thất bại (Key không khớp hoặc dữ liệu lỗi).");
            return null;
        }

        String hashToCheck = SecurityUtils.hashSHA256(rawPassword);

        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, hashToCheck); // So sánh 2 chuỗi Hash với nhau

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                // Tìm thấy User khớp
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setUsername(rs.getString("username"));
                u.setFullName(rs.getString("full_name"));
                // KHÔNG set password trả về để bảo mật
                return u;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null; // Đăng nhập thất bại
    }

    // UserService.java
    public boolean logout(String username) {
        if (username == null || username.isEmpty()) {
            return false;
        }

        // Hiện tại không có session DB
        // Logout chỉ mang ý nghĩa logic server
        return true;
    }


}