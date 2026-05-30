package core.actions.job;

import config.ConfigReader;
import core.context.TestContext;
import core.job.JobExecutionRequest;
import core.job.JobExecutionResponse;
import core.job.JobRequestBuilder;
import core.job.JobType;
import io.qameta.allure.Allure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JobActions implements IJobActions {

    private static final Logger logger = LoggerFactory.getLogger(JobActions.class);

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public void jobEndpointIsAvailable() {
        Allure.step("Check job endpoint is available", () -> {
            try {
                String url = ConfigReader.get("job.base.url")
                        + ConfigReader.get("job.health.endpoint");

                logger.info("[JOB] Checking job endpoint availability | url: {}", url);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

                logger.info(
                        "[JOB] Endpoint availability response | statusCode: {} | body: {}",
                        response.statusCode(),
                        response.body()
                );

                Allure.addAttachment(
                        "Job endpoint availability",
                        "text/plain",
                        """
                        URL: %s
                        HTTP status code: %s
                        Response body: %s
                        """.formatted(url, response.statusCode(), response.body())
                );

                assertEquals(
                        200,
                        response.statusCode(),
                        "Job endpoint is not available: " + url
                );

            } catch (Exception e) {
                logger.error("[JOB] Endpoint availability check failed | reason: {}", e.getMessage(), e);
                throw new RuntimeException("Job endpoint availability check failed", e);
            }
        });
    }

    @Override
    public void runJobWithParameters(String jobType, String jobName, String parameters) {
        Allure.step("Run " + jobType + " " + jobName + " with parameters", () -> {
            try {
                JobType validatedJobType = JobType.from(jobType);
                String finalParameters = resolveDynamicValue(parameters);

                validateJobName(validatedJobType, jobName);
                validateJobParameters(validatedJobType, finalParameters);

                String url = ConfigReader.get("job.base.url")
                        + ConfigReader.get("job.run.endpoint");

                JobExecutionRequest executionRequest = new JobExecutionRequest(
                        validatedJobType.value(),
                        jobName,
                        finalParameters
                );

                String requestBody = JobRequestBuilder.buildRequestBody(executionRequest);

                logger.info(
                        "[JOB] Running job | type: {} | name: {} | url: {}",
                        validatedJobType.value(),
                        jobName,
                        url
                );

                logger.debug("[JOB] Job parameters: {}", finalParameters);
                logger.debug("[JOB] Request body: {}", requestBody);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

                HttpResponse<String> response = httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

                int exitCode = extractExitCode(response.body());

                JobExecutionResponse executionResponse = new JobExecutionResponse(
                        response.statusCode(),
                        response.body(),
                        exitCode
                );

                TestContext.put("jobHttpStatusCode", executionResponse.getHttpStatusCode());
                TestContext.put("jobResponseBody", executionResponse.getResponseBody());
                TestContext.put("jobExitCode", executionResponse.getExitCode());

                logger.info(
                        "[JOB] Job executed | type: {} | name: {} | httpStatusCode: {} | exitCode: {}",
                        validatedJobType.value(),
                        jobName,
                        executionResponse.getHttpStatusCode(),
                        executionResponse.getExitCode()
                );

                Allure.addAttachment(
                        "Job execution details",
                        "text/plain",
                        """
                        URL: %s
                        Job type: %s
                        Job name: %s
                        Parameters: %s
                        HTTP status code: %s
                        Exit code: %s
                        Response body: %s
                        """.formatted(
                                url,
                                validatedJobType.value(),
                                jobName,
                                finalParameters,
                                executionResponse.getHttpStatusCode(),
                                executionResponse.getExitCode(),
                                executionResponse.getResponseBody()
                        )
                );

            } catch (Exception e) {
                logger.error(
                        "[JOB] Job execution failed | type: {} | name: {} | parameters: {} | reason: {}",
                        jobType,
                        jobName,
                        parameters,
                        e.getMessage(),
                        e
                );
                throw new RuntimeException("Job execution failed", e);
            }
        });
    }

    @Override
    public void assertExitCodeEquals() {
        int expectedExitCode = 0 ;
        Allure.step("Assert job exit code is " + expectedExitCode, () -> {
            try {
                Object exitCodeObject = TestContext.get("jobExitCode");

                if (exitCodeObject == null) {
                    throw new RuntimeException(
                            "No job exit code found in TestContext. Please run the job before checking its exit code."
                    );
                }

                int actualExitCode = Integer.parseInt(exitCodeObject.toString());

                logger.info(
                        "[JOB] Asserting job exit code | expected: {} | actual: {}",
                        expectedExitCode,
                        actualExitCode
                );

                Allure.addAttachment(
                        "Job exit code assertion",
                        "text/plain",
                        """
                        Expected exit code: %s
                        Actual exit code: %s
                        Job response body: %s
                        """.formatted(
                                expectedExitCode,
                                actualExitCode,
                                TestContext.getString("jobResponseBody")
                        )
                );

                assertEquals(
                        expectedExitCode,
                        actualExitCode,
                        "Invalid job exit code"
                );

                logger.info("[JOB] Job exit code assertion passed");

            } catch (Exception e) {
                logger.error(
                        "[JOB] Job exit code assertion failed | expected: {} | reason: {}",
                        expectedExitCode,
                        e.getMessage(),
                        e
                );
                throw e;
            }
        });
    }

    private void validateJobName(JobType jobType, String jobName) {
        if (jobName == null || jobName.isBlank()) {
            throw new IllegalArgumentException("Job name is empty");
        }

        if (jobType == JobType.TRANSACTION && !jobName.contains(":")) {
            throw new IllegalArgumentException(
                    "Transaction job must follow format: $package:class.method"
            );
        }

        if (jobType.isScript()
                && !(jobName.endsWith(".sh")
                || jobName.endsWith(".sql")
                || jobName.endsWith(".groovy"))) {
            throw new IllegalArgumentException(
                    "Script job name must end with .sh, .sql or .groovy"
            );
        }
    }

    private void validateJobParameters(JobType jobType, String parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("Job parameters are null");
        }

        if ((jobType == JobType.BATCH || jobType == JobType.TRANSACTION)
                && !(parameters.trim().startsWith("[") && parameters.trim().endsWith("]"))) {
            throw new IllegalArgumentException(
                    "Batch or transaction parameters must use GraphTalk properties format: [parameter=value]"
            );
        }

        if (jobType.isScript() && parameters.isBlank()) {
            throw new IllegalArgumentException("Script parameters cannot be empty");
        }
    }

    private int extractExitCode(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            throw new RuntimeException("Job response body is empty. Cannot extract exitCode.");
        }

        String cleanedBody = responseBody.replaceAll("\\s+", "");
        String key = "\"exitCode\":";

        int keyIndex = cleanedBody.indexOf(key);

        if (keyIndex == -1) {
            throw new RuntimeException("exitCode field not found in response body: " + responseBody);
        }

        int valueStart = keyIndex + key.length();
        int valueEnd = valueStart;

        while (valueEnd < cleanedBody.length()
                && (Character.isDigit(cleanedBody.charAt(valueEnd))
                || cleanedBody.charAt(valueEnd) == '-')) {
            valueEnd++;
        }

        return Integer.parseInt(cleanedBody.substring(valueStart, valueEnd));
    }

    private String resolveDynamicValue(String value) {
        if (value == null) {
            return null;
        }

        String result = value;

        while (result.contains("${")) {
            int start = result.indexOf("${");
            int end = result.indexOf("}", start);

            if (end == -1) {
                break;
            }

            String variableName = result.substring(start + 2, end);
            String variableValue = TestContext.getString(variableName);

            if (variableValue == null) {
                throw new RuntimeException("Variable not found in TestContext: " + variableName);
            }

            result = result.substring(0, start)
                    + variableValue
                    + result.substring(end + 1);
        }

        return result;
    }
}