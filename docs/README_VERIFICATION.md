# Phạm vi xác minh README

Ngày kiểm tra: **29/10/2026**. Dựa trên mã nguồn local, trong đó còn nhiều thay đổi triển khai và test chưa commit. Có màn hình hoặc logic trong source không đồng nghĩa đã xác minh toàn luồng.

## Chạy lại

- Backend: `mvn -f wayway-backend/pom.xml test -B -ntp`: 62 test đạt, 0 lỗi/thất bại. Gồm AI 8, hợp đồng HTTP 16, MoMo cũ 9, outbound client 8, ZaloPay 21.
- Android: `gradlew.bat :app:testDebugUnitTest :app:assembleDebug --no-daemon --console=plain`: build thành công.
- Chạy lại task unit test với up-to-date bị vô hiệu hóa riêng cho task và không dùng build cache: 19 test, 0 lỗi/thất bại/bỏ qua.
- Không có thiết bị ADB kết nối; chưa chạy lại instrumentation hoặc đặt xe toàn luồng trong đợt này.
- Môi trường Java Windows cần thư mục tạm ngắn qua `jdk.net.unixdomain.tmpdir` để xử lý lỗi loopback.

## Nơi đối chiếu mã

Các đường dẫn tính từ `app/src/main/java/com/example/waywayapp/`, trừ khi ghi khác:

| Nội dung              | Đường dẫn                                                                           |
| --------------------- | ----------------------------------------------------------------------------------- |
| Cấu hình provider     | `app/build.gradle.kts` từ gốc repository                                            |
| Bản đồ                | `presentation/user/booking/bike/components/MainMapContent.kt`                       |
| HTTP/OSRM/Photon      | `core/network/RetrofitProvider.kt`, `data/remote/api/`                              |
| Debounce/hủy request  | `presentation/common/AddressSearchController.kt`                                    |
| Cache/rate limit      | `data/repository/AddressSearchRepository.kt`                                        |
| Geocoder              | `data/repository/AndroidAddressGeocoder.kt`                                         |
| Chuyến và transaction | `data/repository/RideRepositoryImpl.kt`                                             |
| Thanh toán            | `wayway-backend/src/main/java/com/example/waywaybackend/zalopay/` từ gốc repository |

Google Maps hiển thị, OSRM lấy tuyến `driving`; Geocoder là autocomplete mặc định, Photon tùy chọn. Không tìm thấy Google Places Autocomplete trong luồng hiện tại. Còn định nghĩa Nominatim cũ nhưng không tìm thấy call site `geocodingApi.` ở lần kiểm tra này.

## Ảnh và kết quả trước đó

Ảnh sao chép nguyên bản từ `build/legacy-ui-final-preview/login.png`, `payment.png`, `build/atm-browser-form.jpg`, `build/atm-gateway-result.jpg` vào `docs/screenshots/`. Hai ảnh đầu là màn hình thật được mở trong bộ kiểm thử UI. Hai ảnh sau là cổng sandbox; kết quả là không thành công.

Báo cáo local trước ghi nhận 8 instrumentation test đạt. Lần thử ATM tạo đơn, query WAITING, mở form và tới OTP giả lập, sau đó cổng báo gián đoạn; chưa có PAID xuyên suốt. Test mock PAID không thay thế kết quả này.

Chưa có benchmark, kiểm thử tải hoặc bằng chứng triển khai backend WayWayApp lên AWS trong đợt xác minh.
