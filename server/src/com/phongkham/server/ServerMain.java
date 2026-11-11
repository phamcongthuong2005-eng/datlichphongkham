package com.phongkham.server;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import org.json.*;

/**
 * SERVER TCP/IP - HỆ THỐNG PHÒNG KHÁM
 * Môn học: Lập Trình Mạng
 * Xử lý: Đăng nhập, Token, Nghiệp vụ đặt lịch, Quản lý Bác sĩ, Phân quyền 4 vai trò.
 */
public class ServerMain {
    public static final int PORT = 8888;
    private static final ExecutorService threadPool = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("       🏥 TCP/IP SERVER - QUẢN LÝ ĐẶT LỊCH PHÒNG KHÁM 🏥       ");
        System.out.println("       Môn học: Lập Trình Mạng                                   ");
        System.out.println("=================================================================");
        
        // Khởi tạo Database Server
        ServerDatabase.init();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("[SERVER READY] Đang lắng nghe kết nối TCP trên Port: " + PORT);
            System.out.println("[IP MÁY CHỦ] IP Local: 127.0.0.1 (Emulator dùng: 10.0.2.2)");
            System.out.println("-----------------------------------------------------------------");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[TCP CONNECT] Client mới kết nối từ: " + clientSocket.getRemoteSocketAddress());
                threadPool.execute(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            System.err.println("[SERVER ERROR] Lỗi ServerSocket: " + e.getMessage());
        }
    }

    // Xử lý từng Client trên 1 Thread riêng biệt
    static class ClientHandler implements Runnable {
        private final Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"))
            ) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    if (inputLine.trim().isEmpty()) continue;
                    System.out.println("\n[TCP RECV ←] " + socket.getRemoteSocketAddress() + ": " + inputLine);

                    JSONObject request = new JSONObject(inputLine);
                    String action = request.optString("action", "");
                    JSONObject data = request.optJSONObject("data");
                    if (data == null) data = new JSONObject();

                    JSONObject response = handleAction(action, data);
                    String responseStr = response.toString();

                    System.out.println("[TCP SEND →] " + socket.getRemoteSocketAddress() + ": " + responseStr);
                    out.write(responseStr);
                    out.newLine();
                    out.flush();
                }
            } catch (Exception e) {
                System.out.println("[TCP DISCONNECT] Client ngắt kết nối: " + socket.getRemoteSocketAddress());
            } finally {
                try { socket.close(); } catch (IOException ignored) {}
            }
        }

        private JSONObject handleAction(String action, JSONObject data) {
            JSONObject res = new JSONObject();
            try {
                switch (action) {
                    case "LOGIN":
                        return ServerDatabase.login(data.optString("account"), data.optString("password"));

                    case "REGISTER":
                        return ServerDatabase.register(data);

                    case "GET_SPECIALTIES":
                        return ServerDatabase.getSpecialties();

                    case "GET_DOCTORS":
                        return ServerDatabase.getDoctors(data.optString("keyword"), data.optInt("specialtyId", 0));

                    case "GET_AVAILABLE_SLOTS":
                        return ServerDatabase.getAvailableSlots(data.optInt("doctorId"), data.optString("date"));

                    case "BOOK_APPOINTMENT":
                        return ServerDatabase.bookAppointment(data);

                    case "GET_PATIENT_APPOINTMENTS":
                        return ServerDatabase.getPatientAppointments(data.optInt("patientId"));

                    case "GET_DOCTOR_APPOINTMENTS":
                        return ServerDatabase.getDoctorAppointments(data.optInt("doctorId"), data.optString("date"));

                    case "GET_ALL_APPOINTMENTS":
                        return ServerDatabase.getAllAppointments(data.optString("date"));

                    case "UPDATE_APPOINTMENT_STATUS":
                        return ServerDatabase.updateStatus(data.optInt("appointmentId"), data.optString("status"));

                    case "CHECK_IN_PATIENT":
                        return ServerDatabase.checkInPatient(data.optInt("appointmentId"), data.optString("date"));

                    case "SAVE_MEDICAL_RECORD":
                        return ServerDatabase.saveMedicalRecord(data);

                    case "GET_MEDICAL_RECORD":
                        return ServerDatabase.getMedicalRecord(data.optInt("appointmentId"));

                    case "CREATE_INVOICE":
                        return ServerDatabase.createInvoice(data);

                    case "ADD_REVIEW":
                        return ServerDatabase.addReview(data);

                    case "GET_AUDIT_LOGS":
                        return ServerDatabase.getAuditLogs();

                    default:
                        res.put("status", "ERROR");
                        res.put("message", "Hành động không hợp lệ: " + action);
                        return res;
                }
            } catch (Exception e) {
                res.put("status", "ERROR");
                res.put("message", "Lỗi xử lý nghiệp vụ Server: " + e.getMessage());
                return res;
            }
        }
    }
}
