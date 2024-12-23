package com.sos.lms.performance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;

import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BookOperationsPerformanceTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private String authToken;

    @BeforeEach
    public void setup() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "oussama");
        loginRequest.put("password", "0000");

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(loginRequest, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity("/authenticate", entity, Map.class);
            if (response.getBody() != null) {
                this.authToken = "Bearer " + response.getBody().get("token");
            }
        } catch (Exception e) {
            fail("Authentication failed: " + e.getMessage());
        }

        if (authToken == null) {
            fail("Failed to obtain authentication token");
        }
    }

    @Test
    public void performBookOperationsLoadTest() {
        int totalOperations = 50;
        int maxConcurrentOperations = 10;
        int timeoutSeconds = 60;

        executePerformanceTest("Load Test", totalOperations, maxConcurrentOperations, timeoutSeconds);
    }

    @Test
    public void performBookOperationsStressTest() {
        int totalOperations = 100;
        int maxConcurrentOperations = 20;
        int timeoutSeconds = 120;

        executePerformanceTest("Stress Test", totalOperations, maxConcurrentOperations, timeoutSeconds);
    }

    private void executePerformanceTest(String testType, int totalOperations,
                                        int maxConcurrentOperations, int timeoutSeconds) {
        List<Long> responseTimes = new CopyOnWriteArrayList<>();
        List<Boolean> operationResults = new CopyOnWriteArrayList<>();
        List<String> operationResponses = new CopyOnWriteArrayList<>();
        Map<String, List<Long>> operationTypeTimings = new ConcurrentHashMap<>();

        ExecutorService executorService = Executors.newFixedThreadPool(maxConcurrentOperations);

        try {
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (int i = 0; i < totalOperations; i++) {
                final int operationId = i;
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    long startTime = System.currentTimeMillis();
                    BookOperationResult result = performBookOperation(operationId);
                    long endTime = System.currentTimeMillis();
                    long duration = endTime - startTime;

                    responseTimes.add(duration);
                    operationResults.add(result.success);
                    operationResponses.add(result.responseBody);

                    operationTypeTimings.computeIfAbsent(result.operationType, k -> new ArrayList<>())
                            .add(duration);
                }, executorService);

                futures.add(future);
            }

            try {
                CompletableFuture<Void> allOf = CompletableFuture.allOf(
                        futures.toArray(new CompletableFuture[0])
                );
                allOf.get(timeoutSeconds, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                System.err.println(testType + " timed out after " + timeoutSeconds + " seconds");
                fail(testType + " exceeded timeout");
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("Error during " + testType + ": " + e.getMessage());
                fail("Error occurred during " + testType + ": " + e.getMessage());
            }

            // Print diagnostic information
            System.out.println("\nBook Operations " + testType + " Diagnostic Information:");
            for (int i = 0; i < operationResponses.size(); i++) {
                System.out.printf("Operation %d: Success=%b, Response=%s%n",
                        i, operationResults.get(i), operationResponses.get(i));
            }

            // Calculate metrics
            long successCount = operationResults.stream().filter(Boolean::booleanValue).count();
            double successRate = (successCount / (double) totalOperations) * 100.0;
            double averageResponseTime = responseTimes.stream()
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(0.0);
            long maxResponseTime = responseTimes.stream()
                    .mapToLong(Long::longValue)
                    .max()
                    .orElse(0L);

            // Calculate metrics by operation type
            System.out.println("\nPerformance Metrics by Operation Type:");
            operationTypeTimings.forEach((type, timings) -> {
                double avgTime = timings.stream().mapToLong(Long::longValue).average().orElse(0.0);
                long maxTime = timings.stream().mapToLong(Long::longValue).max().orElse(0L);
                System.out.printf("%s - Count: %d, Avg Time: %.2f ms, Max Time: %d ms%n",
                        type, timings.size(), avgTime, maxTime);
            });

            // Print performance summary
            System.out.println("\nBook Operations " + testType + " Performance Summary:");
            System.out.printf("Total Operations: %d%n", totalOperations);
            System.out.printf("Maximum Concurrent Operations: %d%n", maxConcurrentOperations);
            System.out.printf("Successful Operations: %d%n", successCount);
            System.out.printf("Success Rate: %.2f%%%n", successRate);
            System.out.printf("Average Response Time: %.2f ms%n", averageResponseTime);
            System.out.printf("Max Response Time: %d ms%n", maxResponseTime);

            // Calculate additional metrics
            long operationsUnder500ms = responseTimes.stream()
                    .filter(time -> time < 500)
                    .count();
            double percentageUnder500ms = (operationsUnder500ms / (double) totalOperations) * 100.0;
            System.out.printf("Operations Under 500ms: %.2f%%%n", percentageUnder500ms);

            // Test-specific assertions
            if (testType.equals("Load Test")) {
                assertTrue(successRate >= 80.0, "At least 80% of load test operations should be successful");
                assertTrue(averageResponseTime < 2000, "Average load test response time should be less than 2 seconds");
                assertTrue(percentageUnder500ms >= 70.0, "At least 70% of operations should complete under 500ms");
            } else {
                assertTrue(successRate >= 75.0, "At least 75% of stress test operations should be successful");
                assertTrue(averageResponseTime < 3000, "Average stress test response time should be less than 3 seconds");
                assertTrue(percentageUnder500ms >= 60.0, "At least 60% of operations should complete under 500ms");
            }

        } finally {
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

    private static class BookOperationResult {
        boolean success;
        String responseBody;
        String operationType;

        BookOperationResult(boolean success, String responseBody, String operationType) {
            this.success = success;
            this.responseBody = responseBody;
            this.operationType = operationType;
        }
    }

    private BookOperationResult performBookOperation(int operationId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", authToken);

            String[] operationTypes = {"GET_ALL", "GET_BY_ID", "CREATE", "UPDATE", "DELETE"};
            String operation = operationTypes[operationId % operationTypes.length];

            ResponseEntity<String> response;
            switch (operation) {
                case "GET_ALL":
                    HttpEntity<Void> getEntity = new HttpEntity<>(headers);
                    response = restTemplate.exchange(
                            "/admin/books",
                            HttpMethod.GET,
                            getEntity,
                            String.class
                    );
                    break;

                case "GET_BY_ID":
                    HttpEntity<Void> getByIdEntity = new HttpEntity<>(headers);
                    response = restTemplate.exchange(
                            "/admin/books/" + (operationId % 10 + 1),
                            HttpMethod.GET,
                            getByIdEntity,
                            String.class
                    );
                    break;

                case "CREATE":
                    Map<String, Object> newBook = new HashMap<>();
                    newBook.put("bookName", "Test Book " + operationId);
                    newBook.put("bookAuthor", "Test Author");
                    newBook.put("bookGenre", "Test Genre");
                    newBook.put("noOfCopies", 5);
                    HttpEntity<Map<String, Object>> createEntity = new HttpEntity<>(newBook, headers);
                    response = restTemplate.exchange(
                            "/admin/books",
                            HttpMethod.POST,
                            createEntity,
                            String.class
                    );
                    break;

                case "UPDATE":
                    Map<String, Object> updatedBook = new HashMap<>();
                    updatedBook.put("bookName", "Updated Book " + operationId);
                    updatedBook.put("bookAuthor", "Updated Author");
                    updatedBook.put("bookGenre", "Updated Genre");
                    updatedBook.put("noOfCopies", 10);
                    HttpEntity<Map<String, Object>> updateEntity = new HttpEntity<>(updatedBook, headers);
                    response = restTemplate.exchange(
                            "/admin/books/" + (operationId % 10 + 1),
                            HttpMethod.PUT,
                            updateEntity,
                            String.class
                    );
                    break;

                case "DELETE":
                    HttpEntity<Void> deleteEntity = new HttpEntity<>(headers);
                    response = restTemplate.exchange(
                            "/admin/books/" + (operationId % 10 + 1),
                            HttpMethod.DELETE,
                            deleteEntity,
                            String.class
                    );
                    break;

                default:
                    throw new IllegalStateException("Unexpected operation: " + operation);
            }

            boolean isSuccessful = response.getStatusCode().is2xxSuccessful();
            return new BookOperationResult(
                    isSuccessful,
                    String.format("%s - %s: %s",
                            operation,
                            response.getStatusCode(),
                            response.getBody() != null ? response.getBody() : "No response body"),
                    operation
            );

        } catch (RestClientException e) {
            System.err.printf("Book operation %d failed: %s%n", operationId, e.getMessage());
            return new BookOperationResult(false, e.getMessage(), "ERROR");
        }
    }
}