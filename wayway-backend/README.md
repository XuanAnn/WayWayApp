# WayWay Backend (MoMo Test)

Backend Spring Boot nho de tao giao dich MoMo test va verify callback (IPN).

## Chay local

PowerShell:

```powershell
$env:MOMO_ENDPOINT="https://test-payment.momo.vn/v2/gateway/api/create"
$env:MOMO_PARTNER_CODE="MOMOBKUN20180529"
$env:MOMO_ACCESS_KEY="klm05TvNBzhg7h7j"
$env:MOMO_SECRET_KEY="at67qH6mk8w5Y1nAyMoYKMWACiEi2bsa"
$env:MOMO_REDIRECT_URL="http://10.0.2.2:8081/api/momo/return"
$env:MOMO_IPN_URL="http://10.0.2.2:8081/api/momo/ipn"

cd wayway-backend
./mvnw.cmd spring-boot:run
```

## API

- `POST /api/momo/create`: tao order, tra ve `payUrl`/`deeplink` de app mo MoMo.
- `POST /api/momo/ipn`: MoMo goi ve (server-to-server) de thong bao ket qua thanh toan.
- `GET /api/momo/return`: redirect sau khi user xac nhan tren app/website MoMo (debug).
- `GET /api/momo/orders/{orderId}`: kiem tra trang thai order (in-memory).

## Luu y ve verify that

- `redirectUrl` chi de debug UI/return; trang thai thanh toan nen duoc tin tuong qua `ipnUrl` (chu ky hop le).
- Khi test tren emulator Android: dung `10.0.2.2` de goi ve may host. IPN tu MoMo test server goi ve URL public, nen neu muon nhan IPN that ban can expose backend ra internet (VD: ngrok/cloud).

