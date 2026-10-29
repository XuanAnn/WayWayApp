# WayWay Backend

Java 17, Spring Boot 3.3 WebFlux: API ZaloPay sandbox và trợ lý AI. Dữ liệu chuyến, chat và hồ sơ được Android thao tác qua Firebase. Xem [README dự án](../README.md) để cấu hình và đọc kết quả kiểm tra.

Tài liệu mô tả bản local ngày 29/10/2026; một số triển khai sandbox/test còn chưa commit.

## Chạy từ thư mục gốc repository

```powershell
mvn -f wayway-backend/pom.xml spring-boot:run
mvn -f wayway-backend/pom.xml test
```

Mặc định cổng 8081, profile sandbox. Cần Maven và JDK phù hợp Java release 17.

| Biến môi trường                                  | Mục đích                                                              |
| ------------------------------------------------ | --------------------------------------------------------------------- |
| `SERVER_PORT`                                    | Cổng HTTP.                                                            |
| `ZALOPAY_APP_ID`, `ZALOPAY_KEY1`, `ZALOPAY_KEY2` | Merchant sandbox; khóa chỉ giữ phía server.                           |
| `ZALOPAY_CALLBACK_URL`, `ZALOPAY_RETURN_URL`     | URL HTTPS nếu nhận callback/return.                                   |
| `ZALOPAY_STORAGE_FILE`                           | File JSON lưu đơn; cần giữ qua các lần khởi động.                     |
| `AI_PROVIDER`                                    | Mặc định OLLAMA.                                                      |
| `OLLAMA_ENDPOINT`, `OLLAMA_MODEL`                | Mặc định localhost:11434 và qwen2.5:1.5b.                             |
| `OPENAI_API_KEY`, `OPENAI_MODEL`                 | Adapter OpenAI khi chọn provider tương ứng; không đặt khóa trong APK. |

## API chính

| Method | Đường dẫn                  | Vai trò                                |
| ------ | -------------------------- | -------------------------------------- |
| POST   | `/api/zalopay/create`      | Tạo đơn checkout sandbox.              |
| POST   | `/api/zalopay/callback`    | Xác minh MAC và thông tin callback.    |
| GET    | `/api/zalopay/orders/{id}` | Kiểm tra đơn/query trạng thái cổng.    |
| GET    | `/api/zalopay/return`      | Quay về; không tự xác nhận thanh toán. |
| POST   | `/api/ai/chat`             | Trợ lý theo nội dung/ngữ cảnh.         |

MoMo cũ vẫn có source/test nhưng mặc định tắt. Luồng thanh toán hiện mô tả là ZaloPay ATM sandbox.

Ngày 29/10/2026: 62 test backend đạt. Test mock kiểm tra API, chữ ký, callback lặp, lỗi và trạng thái; chưa xác nhận PAID xuyên suốt trên cổng thật. File JSON phù hợp demo một instance. Cần hoàn thiện xác thực, ràng buộc giá/người dùng/chuyến và lưu trữ trước khi thu tiền thật.
