package server;

import java.net.ServerSocket;
import java.net.Socket;

public class ServerMain {
    public static void main(String[] args) {

        int PORT = 9999;

        if (DatabaseConnection.getConnection() == null) {
            System.err.println(" Không thể khởi động Server vì lỗi Database.");
            return;
        }

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("==========================================");
            System.out.println("🚀 MAIL SERVER ĐANG CHẠY TẠI PORT " + PORT);
            System.out.println("==========================================");

            while (true) {
                // Chấp nhận kết nối
                Socket clientSocket = serverSocket.accept();
                System.out.println("👤 Client mới kết nối từ: " + clientSocket.getInetAddress());

                Thread t = new Thread(new ClientHandler(clientSocket));
                t.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}