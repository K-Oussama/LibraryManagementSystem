package com.sos.lms.performance;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LoginPerformanceTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void performLoginStressTest() {
        // Configuration
        int totalUsers = 50;
        int maxConcurrentUsers = 10;
        int timeoutSeconds = 60;

        // Performance metrics tracking
        List<Long> responseTimes = new CopyOnWriteArrayList<>();
        List<Boolean> loginResults = new CopyOnWriteArrayList<>();
        List<String> loginResponses = new CopyOnWriteArrayList<>();

        // Create a thread pool
        ExecutorService executorService = Executors.newFixedThreadPool(maxConcurrentUsers);

        try {
            // Perform concurrent login attempts
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (int i = 0; i < totalUsers; i++) {
                final int userId = i;
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    long startTime = System.currentTimeMillis();
                    LoginAttemptResult result = performDetailedLogin(userId);
                    long endTime = System.currentTimeMillis();

                    responseTimes.add(endTime - startTime);
                    loginResults.add(result.success);
                    loginResponses.add(result.responseBody);
                }, executorService);

                futures.add(future);
            }

            // Wait for all tasks to complete
            try {
                CompletableFuture<Void> allOf = CompletableFuture.allOf(
                        futures.toArray(new CompletableFuture[0])
                );
                allOf.get(timeoutSeconds, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                System.err.println("Login stress test timed out after " + timeoutSeconds + " seconds");
                fail("Login stress test exceeded timeout");
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("Error during login stress test: " + e.getMessage());
                fail("Error occurred during login stress test: " + e.getMessage());
            }

            // Print out some diagnostic information
            System.out.println("\nLogin Diagnostic Information:");
            for (int i = 0; i < loginResponses.size(); i++) {
                System.out.printf("Login %d: Success=%b, Response=%s%n",
                        i, loginResults.get(i), loginResponses.get(i));
            }

            // Calculate performance metrics
            long successCount = loginResults.stream().filter(Boolean::booleanValue).count();
            double successRate = (successCount / (double) totalUsers) * 100.0;
            double averageResponseTime = responseTimes.stream()
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(0.0);
            long maxResponseTime = responseTimes.stream()
                    .mapToLong(Long::longValue)
                    .max()
                    .orElse(0L);

            // Print performance summary
            System.out.println("\nLogin Performance Summary:");
            System.out.printf("Total Login Attempts: %d%n", totalUsers);
            System.out.printf("Successful Logins: %d%n", successCount);
            System.out.printf("Success Rate: %.2f%%%n", successRate);
            System.out.printf("Average Response Time: %.2f ms%n", averageResponseTime);
            System.out.printf("Max Response Time: %d ms%n", maxResponseTime);

            // Assertions
            assertTrue(successRate >= 80.0, "At least 80% of login attempts should be successful");
            assertTrue(averageResponseTime < 2000, "Average login response time should be less than 2 seconds");

        } finally {
            // Shutdown the executor service
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
            }
        }
    }

    private static class LoginAttemptResult {
        boolean success;
        String responseBody;

        LoginAttemptResult(boolean success, String responseBody) {
            this.success = success;
            this.responseBody = responseBody;
        }
    }

    private LoginAttemptResult performDetailedLogin(int userId) {
        try {
            // Prepare login request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> loginRequest = new HashMap<>();
            loginRequest.put("username", "oussama");  // Use your actual test credentials
            loginRequest.put("password", "0000");

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(loginRequest, headers);

            // Perform login
            ResponseEntity<String> response = restTemplate.postForEntity(
                    "/authenticate",
                    entity,
                    String.class
            );

            // Check if login was successful
            boolean isSuccessful = response.getStatusCode().is2xxSuccessful();

            return new LoginAttemptResult(
                    isSuccessful,
                    response.getBody() != null ? response.getBody() : "No response body"
            );
        } catch (RestClientException e) {
            System.err.printf("Login attempt %d failed: %s%n", userId, e.getMessage());
            return new LoginAttemptResult(false, e.getMessage());
        }
    }
}