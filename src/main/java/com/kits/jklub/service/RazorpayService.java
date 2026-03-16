package com.kits.jklub.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RazorpayService {

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    /**
     * Creates an order for ₹100 + fees (~₹102.42 total).
     */
    public String createSubscriptionOrder(String loginIdentifier) throws RazorpayException {
        RazorpayClient client = new RazorpayClient(keyId, keySecret);

        double baseAmount = 100.0;
        // Total = 100 / (1 - 0.0236) to cover 2% fee + 18% GST
        int amountInPaise = (int) Math.round((baseAmount / (1 - 0.0236)) * 100);

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amountInPaise);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "rcpt_" + loginIdentifier);

        JSONObject notes = new JSONObject();
        notes.put("loginIdentifier", loginIdentifier);
        orderRequest.put("notes", notes);

        Order order = client.orders.create(orderRequest);
        return order.toString();
    }

    /**
     * Verifies if the payment signature is authentic.
     */
    public boolean verifyPaymentSignature(String orderId, String paymentId, String signature) {
        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", orderId);
            options.put("razorpay_payment_id", paymentId);
            options.put("razorpay_signature", signature);

            return Utils.verifyPaymentSignature(options, keySecret);
        } catch (Exception e) {
            return false;
        }
    }
}