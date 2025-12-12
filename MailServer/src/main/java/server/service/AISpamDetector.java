package server.service;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AISpamDetector {
    private static final String API_KEY = "AIzaSyD-cvYtPHKAHgf_CuXPNDUhSRQ6b20keuw";
    private static final String MODEL_NAME = "gemini-2.0-flash-lite-preview-02-05";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/" + MODEL_NAME + ":generateContent?key=" + API_KEY;

    public static boolean checkSpam(String subject, String body) {
        // Chuẩn bị nội dung cho bộ lọc dự phòng
        String content = (subject + " " + body).toUpperCase();

        try {
            String prompt = "Phân tích email này. Nếu là spam/lừa đảo/quảng cáo trả lời YES. Nếu không trả lời NO. Subject: " + subject + ". Body: " + body;
            JSONObject payload = new JSONObject().put("contents", new JSONArray().put(new JSONObject().put("parts", new JSONArray().put(new JSONObject().put("text", prompt)))));

            // Gửi Request
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                    .build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            String responseBody = res.body();
            JSONObject jsonRes = new JSONObject(responseBody);

            if (jsonRes.has("error")) {
                String errorMsg = jsonRes.getJSONObject("error").getString("message");
                System.out.println(" Lỗi Google API (" + MODEL_NAME + "): " + errorMsg);
                throw new Exception("API Error"); // Ném lỗi để nhảy xuống bộ lọc từ khóa
            }

            // Đọc kết quả
            String ans = jsonRes.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
                    .toUpperCase();

            System.out.println(" AI Check (" + MODEL_NAME + "): " + ans);
            return ans.contains("YES");

        } catch (Exception e) {

            System.out.println(" Chuyển sang chế độ lọc Từ khóa (Keyword Fallback).");

            String[] spamKeywords = {
                    // 1. Nhóm Lừa đảo / Tiền bạc
                    "TRÚNG THƯỞNG", "1 TỶ", "100 TRIỆU", "CHUYỂN KHOẢN", "NHẬN TIỀN",
                    "VAY VỐN", "VAY TIỀN", "LÃI SUẤT 0%", "GIẢI NGÂN", "NỢ XẤU",
                    "KIẾM TIỀN", "VIỆC NHẸ LƯƠNG CAO", "THU NHẬP THỤ ĐỘNG",

                    // 2. Nhóm Cờ bạc / Cá độ (Rất phổ biến ở VN)
                    "NHÀ CÁI", "CÁ CƯỢC", "CÁ ĐỘ", "TÀI XỈU", "NỔ HŨ",
                    "BẮN CÁ", "XỔ SỐ", "LÔ ĐỀ", "KU CASINO", "BET88",

                    // 3. Nhóm Quảng cáo / Bán hàng
                    "KHUYẾN MÃI", "GIẢM GIÁ", "FREE", "MIỄN PHÍ",
                    "QUÀ TẶNG", "IPHONE", "SHOCK", "XẢ KHO",

                    // 4. Nhóm Hành động khẩn cấp (Phishing)
                    "CLICK VÀO", "NHẤP VÀO", "TẠI ĐÂY", "LINK DƯỚI",
                    "XÁC MINH", "BỊ KHÓA", "MẬT KHẨU", "MÃ OTP",
                    "LỪA ĐẢO", "SPAM", "HACK"
            };

            for (String keyword : spamKeywords) {
                if (content.contains(keyword)) {
                    System.out.println("Keyword Check: PHÁT HIỆN SPAM (Từ khóa: " + keyword + ")");
                    return true;
                }
            }

            System.out.println(" Keyword Check: OK (An toàn)");
            return false;
        }
    }
}