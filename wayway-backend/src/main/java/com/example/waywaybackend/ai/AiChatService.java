package com.example.waywaybackend.ai;

import com.example.waywaybackend.ai.api.AiChatRequest;
import com.example.waywaybackend.ai.api.AiChatResponse;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.text.Normalizer;
import java.util.Locale;

@Service
// Service xử lý câu hỏi AI, ưu tiên trả lời dữ liệu chắc chắn trước khi gọi model.
public class AiChatService {
    // Client gọi OpenAI khi cấu hình provider dùng OpenAI.
    private final OpenAiClient openAiClient;
    // Client gọi Ollama local để chạy AI miễn phí trên máy.
    private final OllamaClient ollamaClient;
    // Cấu hình chọn provider, model và endpoint AI.
    private final AiProperties props;

    public AiChatService(
            OpenAiClient openAiClient,
            OllamaClient ollamaClient,
            AiProperties props
    ) {
        this.openAiClient = openAiClient;
        this.ollamaClient = ollamaClient;
        this.props = props;
    }

    public Mono<AiChatResponse> chat(
            AiChatRequest request
    ) {
        String message = request.message().trim();
        // Trả lời trực tiếp bằng context cho câu hỏi giá cước/mã giảm giá để tránh AI bịa.
        String deterministicReply = deterministicContextReply(message, request.context());
        if (deterministicReply != null) {
            return Mono.just(new AiChatResponse(deterministicReply, "context"));
        }

        // Nếu cấu hình dùng Ollama thì gửi câu hỏi sang model local.
        if ("OLLAMA".equalsIgnoreCase(props.provider())) {
            return chatWithOllama(request, message);
        }

        // Nếu không có OpenAI key thì tự chuyển sang Ollama.
        if (!openAiClient.isConfigured()) {
            return chatWithOllama(request, message);
        }

        // Gọi OpenAI thật khi backend có key và provider cho phép.
        return openAiClient.chat(systemPrompt(request.role(), request.locale(), request.context()), message)
                .map(reply -> new AiChatResponse(reply, "openai"))
                .onErrorResume(error -> {
                    System.err.println("OpenAI chat failed: " + error.getMessage());
                    if ("AUTO".equalsIgnoreCase(props.provider())) {
                        return chatWithOllama(request, message);
                    }
                    return demoResponse(message, request.role(), "fallback");
                });
    }

    private Mono<AiChatResponse> chatWithOllama(
            AiChatRequest request,
            String message
    ) {
        // Gửi prompt và message sang Ollama local qua /api/chat.
        return ollamaClient.chat(systemPrompt(request.role(), request.locale(), request.context()), message)
                .map(reply -> new AiChatResponse(reply, "ollama"))
                .onErrorResume(error -> {
                    System.err.println("Ollama chat failed: " + error.getMessage());
                    return demoResponse(message, request.role(), "demo");
                });
    }

    private Mono<AiChatResponse> demoResponse(
            String message,
            String role,
            String mode
    ) {
        // Fallback rule-based để app vẫn trả lời được khi AI local/API lỗi.
        return Mono.just(new AiChatResponse(demoReply(message, role), mode));
    }

    // Tạo system prompt kèm role và context thật từ app để định hướng AI.
    private String systemPrompt(
            String role,
            String locale,
            JsonNode context
    ) {
        String userRole = role == null || role.isBlank() ? "USER" : role.toUpperCase(Locale.ROOT);
        String language = locale == null || locale.isBlank() ? "vi-VN" : locale;
        return """
                Ban la tro ly AI trong ung dung dat xe WayWay.
                Tra loi bang tieng Viet, ngan gon, than thien, uu tien hanh dong ro rang.
                Ngu canh nguoi dung hien tai: %s. Locale: %s.
                Ban co the ho tro: cach dat xe, thanh toan MoMo/tien mat, theo doi tai xe,
                lien he tai xe/khach, lich su cuoc, ho so, vi tai xe va xu ly loi co ban.
                Neu cau hoi can thong tin rieng tu hoac giao dich that, hay huong dan mo man hinh phu hop trong app.
                Du lieu thoi gian thuc tu app/backend:
                %s
                Neu activePromos rong, hay noi hien chua co ma giam gia kha dung. Khong tu tao ma giam gia.
                Neu currentFare null, hay noi can chon diem don/diem den de app tinh gia. Khong tu bao gia chinh xac.
                Neu currentFare co du lieu, chi tra loi gia cuoc theo currentFare.
                Khong tu y tao cam ket phap ly, y te, tai chinh.
                """.formatted(userRole, language, contextSummary(context));
    }

    private String contextSummary(
            JsonNode context
    ) {
        // Chuyển context JSON sang text để model đọc được mã giảm giá/giá cước hiện tại.
        if (context == null || context.isNull() || context.isMissingNode()) {
            return "context: null";
        }
        return context.toPrettyString();
    }

    private String deterministicContextReply(
            String message,
            JsonNode context
    ) {
        // Chuẩn hóa tiếng Việt không dấu để bắt intent đơn giản.
        String normalized = Normalizer.normalize(message, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);

        if (normalized.contains("ma giam") || normalized.contains("khuyen mai") || normalized.contains("promo")) {
            // Lấy danh sách activePromos do Android gửi lên từ Firestore.
            JsonNode promos = context == null ? null : context.path("activePromos");
            if (promos == null || !promos.isArray() || promos.isEmpty()) {
                return "Hien chua co ma giam gia kha dung cho dich vu nay.";
            }

            StringBuilder builder = new StringBuilder("Hom nay co cac ma giam gia kha dung:\n");
            for (JsonNode promo : promos) {
                String code = promo.path("code").asText("");
                String title = promo.path("title").asText("");
                String discountType = promo.path("discountType").asText("");
                int discountValue = promo.path("discountValue").asInt(0);
                int minFare = promo.path("minFare").asInt(0);
                int maxDiscount = promo.path("maxDiscount").asInt(0);

                builder.append("- ").append(code);
                if (!title.isBlank()) builder.append(": ").append(title);
                if ("fixed".equalsIgnoreCase(discountType)) {
                    builder.append(" giam ").append(formatVnd(discountValue));
                } else if ("percent".equalsIgnoreCase(discountType)) {
                    builder.append(" giam ").append(discountValue).append("%");
                }
                if (maxDiscount > 0) builder.append(", toi da ").append(formatVnd(maxDiscount));
                if (minFare > 0) builder.append(", don tu ").append(formatVnd(minFare));
                builder.append("\n");
            }
            return builder.toString().trim();
        }

        if (normalized.contains("gia") || normalized.contains("cuoc") || normalized.contains("bao nhieu")) {
            // Lấy currentFare nếu app đang có giá cước từ màn đặt xe.
            JsonNode fare = context == null ? null : context.path("currentFare");
            if (fare == null || fare.isNull() || fare.isMissingNode()) {
                return "Minh chua co gia cuoc chinh xac. Ban hay chon diem don va diem den, app se tinh gia tam tinh cho ban.";
            }

            String serviceType = fare.path("serviceType").asText("bike");
            double estimatedFare = fare.path("estimatedFare").asDouble(0.0);
            double finalFare = fare.path("finalFare").asDouble(estimatedFare);
            double discount = fare.path("discount").asDouble(0.0);
            String promoCode = fare.path("selectedPromoCode").asText("");

            StringBuilder builder = new StringBuilder();
            builder.append("Gia cuoc ").append(serviceType).append(" tam tinh la ")
                    .append(formatVnd(estimatedFare)).append(".");
            if (discount > 0.0) {
                builder.append(" Da giam ").append(formatVnd(discount));
                if (!promoCode.isBlank()) builder.append(" voi ma ").append(promoCode);
                builder.append(", con ").append(formatVnd(finalFare)).append(".");
            }
            return builder.toString();
        }

        return null;
    }

    private String formatVnd(
            double value
    ) {
        // Định dạng tiền Việt Nam để câu trả lời dễ đọc.
        return String.format(new Locale("vi", "VN"), "%,.0fđ", value);
    }

    // Trả lời theo luật cố định cho các câu hỏi thường gặp khi model không dùng được.
    private String demoReply(
            String message,
            String role
    ) {
        String normalized = Normalizer.normalize(message, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
        String normalizedRole = role == null ? "USER" : role.toUpperCase(Locale.ROOT);

        if ("DRIVER".equals(normalizedRole)) {
            if (normalized.contains("thu nhap") || normalized.contains("kiem") || normalized.contains("vi")) {
                return "Ban bam nut vi o goc tren man hinh driver de xem so du, so cuoc da hoan thanh va giao dich gan day. Moi cuoc hoan thanh se duoc cong vao vi.";
            }
            if (normalized.contains("nhan") || normalized.contains("cuoc")) {
                return "De nhan nhieu cuoc hon, hay bat ket noi, dung gan khu vuc dong khach, giu vi tri cap nhat on dinh va phan hoi nhanh khi co cuoc moi.";
            }
            if (normalized.contains("khach") || normalized.contains("goi") || normalized.contains("nhan tin")) {
                return "Neu khach khong nghe may, ban nen nhan tin trong app, goi lai mot lan nua va chi bam 'Da toi' khi ban that su o diem don.";
            }
            if (normalized.contains("trang thai") || normalized.contains("hoan thanh")) {
                return "Luong chuyen cua tai xe la: nhan cuoc, den diem don, bat dau chuyen, den diem tra, hoan thanh. Moi lan doi trang thai app se cap nhat cho khach.";
            }
            return "Minh co the ho tro tai xe ve nhan cuoc, dieu huong, lien he khach, vi, thu nhap va cac trang thai chuyen di.";
        }

        if (normalized.contains("momo") || normalized.contains("thanh toan")) {
            return "Ban co the chon tien mat hoac MoMo o man xac nhan cuoc. Neu dung MoMo UAT, hay mo MoMo, thanh toan xong quay lai app va bam kiem tra trang thai.";
        }
        if (normalized.contains("ma giam") || normalized.contains("khuyen mai") || normalized.contains("promo")) {
            return "Hien app dang co khu vuc uu dai tren trang chu. Ban co the xem banner uu dai hoac nhap ma neu man xac nhan cuoc hien o khuyen mai.";
        }
        if (normalized.contains("gia") || normalized.contains("cuoc") || normalized.contains("bao nhieu")) {
            return "Gia cuoc duoc tinh tu khoang cach, loai dich vu va thoi diem dat xe. Sau khi chon diem don/diem den, app se hien gia tam tinh truoc khi ban xac nhan.";
        }
        if (normalized.contains("tai xe") || normalized.contains("driver")) {
            return "Khi tai xe nhan cuoc, app se hien vi tri tai xe, trang thai chuyen di, nut goi dien va nhan tin.";
        }
        if (normalized.contains("lich su") || normalized.contains("danh gia")) {
            return "Vao muc Lich su, bam vao cuoc da hoan thanh de xem chi tiet va danh gia tai xe.";
        }
        if (normalized.contains("vi") || normalized.contains("thu nhap")) {
            return "Tai xe co the bam nut vi o goc tren man hinh driver de xem so du va cac cuoc da hoan thanh.";
        }
        return "Minh co the ho tro ban ve dat xe, thanh toan, theo doi tai xe, lich su cuoc, ho so va vi tai xe. Ban muon lam gi tiep?";
    }
}
