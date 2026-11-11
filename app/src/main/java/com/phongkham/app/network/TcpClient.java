package com.phongkham.app.network;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * TCP CLIENT CHO ANDROID (MÔN LẬP TRÌNH MẠNG)
 * Giao tiếp Socket TCP/IP với Java Server qua cổng 8888
 * Emulator IP: 10.0.2.2 | Thiết bị thật: IP mạng LAN của máy tính
 */
public class TcpClient {
    private static final String TAG = "TcpClient";
    public static String SERVER_IP = "10.0.2.2"; // 10.0.2.2 kết nối localhost từ máy ảo Android
    public static int SERVER_PORT = 8888;
    private static final int TIMEOUT_MS = 3000;

    private static final ExecutorService networkExecutor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface ResponseCallback {
        void onSuccess(JSONObject response);
        void onError(String errorMessage);
    }

    public static void sendRequest(String action, JSONObject data, ResponseCallback callback) {
        networkExecutor.execute(() -> {
            Socket socket = null;
            try {
                JSONObject request = new JSONObject();
                request.put("action", action);
                request.put("data", data != null ? data : new JSONObject());

                Log.d(TAG, "[TCP SEND → " + SERVER_IP + ":" + SERVER_PORT + "] " + request.toString());

                socket = new Socket();
                socket.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT), TIMEOUT_MS);
                socket.setSoTimeout(TIMEOUT_MS);

                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));

                // Gửi bản tin qua TCP Socket kết thúc bằng newline
                out.write(request.toString());
                out.newLine();
                out.flush();

                // Đọc phản hồi từ Server
                String responseLine = in.readLine();
                Log.d(TAG, "[TCP RECV ←] " + responseLine);

                if (responseLine != null && !responseLine.trim().isEmpty()) {
                    JSONObject resObj = new JSONObject(responseLine);
                    mainHandler.post(() -> {
                        if ("SUCCESS".equals(resObj.optString("status"))) {
                            callback.onSuccess(resObj);
                        } else {
                            callback.onError(resObj.optString("message", "Lỗi phản hồi từ server"));
                        }
                    });
                } else {
                    mainHandler.post(() -> callback.onError("Không nhận được phản hồi từ Server TCP"));
                }
            } catch (Exception e) {
                Log.w(TAG, "Lỗi kết nối Server TCP: " + e.getMessage());
                mainHandler.post(() -> callback.onError("Không thể kết nối đến Server TCP (" + SERVER_IP + ":" + SERVER_PORT + "). Vui lòng bật Server!"));
            } finally {
                if (socket != null) {
                    try { socket.close(); } catch (Exception ignored) {}
                }
            }
        });
    }
}
