package com.white.apidoc.payment.vnpay;

import com.white.apidoc.core.config.payment.VNPAYConfig;
import com.white.apidoc.util.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    private final VNPAYConfig vnPayConfig;
    private final WebClient webClient;

    public PaymentDTO.VNPayResponse createVnPayPayment(HttpServletRequest request) {
        long amount = Integer.parseInt(request.getParameter("amount")) * 100L;
        String bankCode = request.getParameter("bankCode");
        String invoiceId = request.getHeader("X-INVOICE-ID");
        Map<String, String> vnpParamsMap = vnPayConfig.getVNPayConfig(invoiceId);
        vnpParamsMap.put("vnp_Amount", String.valueOf(amount));
        if (bankCode != null && !bankCode.isEmpty()) {
            vnpParamsMap.put("vnp_BankCode", bankCode);
        }
        vnpParamsMap.put("vnp_IpAddr", VNPayUtil.getIpAddress(request));
        //build query url
        String queryUrl = VNPayUtil.getPaymentURL(vnpParamsMap, true);
        String hashData = VNPayUtil.getPaymentURL(vnpParamsMap, false);
        String vnpSecureHash = VNPayUtil.hmacSHA512(vnPayConfig.getSecretKey(), hashData);
        queryUrl += "&vnp_SecureHash=" + vnpSecureHash;
        String paymentUrl = vnPayConfig.getVnp_PayUrl() + "?" + queryUrl;
        return PaymentDTO.VNPayResponse.builder()
                .code("ok")
                .message("success")
                .paymentUrl(paymentUrl).build();
    }

    public PaymentDTO.VNPayResponse handlePaymentCallback(String status, String message, String url, BigDecimal amount, String invoiceId) {
        try {
            webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/invoices/{invoiceId}/record-payment")
                            .queryParam("amount", amount)
                            .build(invoiceId)
                    )
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (Exception e) {
            log.error("Failed to record payment for invoice {}: {}", invoiceId, e.getMessage());
            throw new RuntimeException("Failed to record payment: " + e.getMessage(), e);
        }

        return PaymentDTO.VNPayResponse.builder()
                .code(status)
                .message(message)
                .paymentUrl(url).build();
    }
}
