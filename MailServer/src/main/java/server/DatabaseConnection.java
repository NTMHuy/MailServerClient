package server;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/DACS4";
    private static final String USER = "postgres";
    private static final String PASS = "123";


    public static Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            System.out.println("Kết nối PostgreSQL thành công!");

        } catch (ClassNotFoundException e) {
            System.err.println("Lỗi: Không tìm thấy Driver PostgreSQL. Kiểm tra lại thư mục lib!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Lỗi: Sai thông tin kết nối (URL, User hoặc Pass)!");
            e.printStackTrace();
        }
        return conn;
    }

    public static void main(String[] args) {
        getConnection();
    }
}