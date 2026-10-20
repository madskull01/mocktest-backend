package com.example.mocktest.service.payment;

import com.example.mocktest.exception.BadRequestException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;

@Component
public class RazorpayGateway {

    private static final String API_BASE_URL = "https://api.razorpay.com/v1";

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String keyId;
    private final String keySecret;

    public RazorpayGateway(
            @Value("${app.razorpay.key-id:}") String keyId,
            @Value("${app.razorpay.key-secret:}") String keySecret) {
        this.objectMapper = new ObjectMapper();
        this.keyId = keyId;
        this.keySecret = keySecret;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public String getKeyId() {
        return keyId;
    }

    public JsonNode createOrder(long amountInPaise, String receipt) {
        requireCredentials();
        try {
            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "amount", amountInPaise,
                    "currency", "INR",
                    "receipt", receipt
            ));
            HttpRequest request = authorizedRequest("/orders")
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            return send(request);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not create Razorpay order", exception);
        }
    }

    public JsonNode fetchPayment(String paymentId) {
        requireCredentials();
        HttpRequest request = authorizedRequest("/payments/" + paymentId)
                .GET()
                .build();
        return send(request);
    }

    private HttpRequest.Builder authorizedRequest(String path) {
        String credentials = keyId + ":" + keySecret;
        String authorization = Base64.getEncoder().encodeToString(
                credentials.getBytes(StandardCharsets.UTF_8));
        return HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + path))
                .timeout(Duration.ofSeconds(15))
                .header("Authorization", "Basic " + authorization)
                .header("Accept", "application/json");
    }

    private JsonNode send(HttpRequest request) {
        try {
            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException(
                        "Razorpay API returned HTTP " + response.statusCode());
            }
            return objectMapper.readTree(response.body());
        } catch (IOException exception) {
            throw new IllegalStateException("Razorpay API request failed", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Razorpay API request was interrupted", exception);
        }
    }

    private void requireCredentials() {
        if (keyId == null || keyId.isBlank()
                || keySecret == null || keySecret.isBlank()) {
            throw new BadRequestException(
                    "Razorpay is not configured. Set RAZORPAY_KEY_ID and RAZORPAY_KEY_SECRET.");
        }
    }
}
