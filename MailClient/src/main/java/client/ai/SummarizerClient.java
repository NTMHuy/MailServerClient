package client.ai;

import okhttp3.*;
import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;

public class SummarizerClient {

    private static final OkHttpClient client = new OkHttpClient();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final Gson gson = new Gson();

    public static String summarize(String text) {
        try {
            Map<String, String> map = new HashMap<>();
            map.put("text", text);

            String json = gson.toJson(map);

            RequestBody body = RequestBody.create(json, JSON);
            Request request = new Request.Builder()
                    .url("http://127.0.0.1:5000/summary")
                    .post(body)
                    .build();

            Response response = client.newCall(request).execute();
            String result = response.body().string();

            // Parse JSON trả về
            Map<?, ?> res = gson.fromJson(result, Map.class);
            return res.get("summary").toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "⚠ Không thể kết nối AI Server!";
        }
    }
}
