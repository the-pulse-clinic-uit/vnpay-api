package com.white.apidoc.payment.vnpay;

import com.white.apidoc.core.response.ResponseObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.math.BigDecimal;

@RestController
@RequestMapping("${spring.application.api-prefix}/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    @GetMapping("/vn-pay")
    public ResponseObject<PaymentDTO.VNPayResponse> pay(HttpServletRequest request) {
        return new ResponseObject<>(HttpStatus.OK, "Success", paymentService.createVnPayPayment(request));
    }
    @GetMapping("/vn-pay-callback")
    public void payCallbackHandler(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String status = request.getParameter("vnp_ResponseCode");
            String amountParam = request.getParameter("vnp_Amount");
            String txnRef = request.getParameter("vnp_TxnRef");

            if (status == null || amountParam == null || txnRef == null) {
                response.sendRedirect("http://localhost:3000/invoices/failure?error=missing_parameters");
                return;
            }

            BigDecimal amount = new BigDecimal(amountParam);
            String invoiceId = txnRef.split("_vnp_")[0];

            if (status.equals("00")) {
                // record the payment in backend
                paymentService.handlePaymentCallback("00", "Success", "", amount, invoiceId);
                // redirect to frontend success page
                response.sendRedirect("http://localhost:3000/invoices/success?invoiceId=" + invoiceId + "&amount=" + amount);
            } else {
                // redirect to frontend failure page
                response.sendRedirect("http://localhost:3000/invoices/failure?invoiceId=" + invoiceId + "&status=" + status);
            }
        } catch (Exception e) {
            // Log the error and redirect to failure page
            e.printStackTrace();
            response.sendRedirect("http://localhost:3000/invoices/failure?error=" + e.getMessage());
        }
    }
}
