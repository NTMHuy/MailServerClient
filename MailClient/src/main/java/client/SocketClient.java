package client;


import common.Request;
import common.Response;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SocketClient {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    // Địa chỉ Server (localhost vì chạy chung máy)
    private static final String HOST = "localhost";
    private static final int PORT = 9999;

    public void connect() {
        try {
            // Chỉ kết nối nếu chưa kết nối hoặc đã bị đóng
            if (socket == null || socket.isClosed()) {
                socket = new Socket(HOST, PORT);

                // Tạo luồng gửi/nhận (Output trước Input)
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());

                System.out.println("✅ Đã kết nối đến Server " + HOST + ":" + PORT);
            }
        } catch (Exception e) {
            System.err.println(" Không tìm thấy Server! Hãy chắc chắn bạn đã chạy ServerMain.");
        }
    }

    public Response sendRequest(Request request) {
        try {
            if (socket == null) connect();

            // 1. Gửi yêu cầu
            out.writeObject(request);
            out.flush();

            // 2. Chờ nhận phản hồi
            return (Response) in.readObject();

        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Lỗi kết nối: " + e.getMessage(), null);
        }
    }

    public void close() {
        try {
            if (socket != null) socket.close();
        } catch (Exception e) {}
    }
}