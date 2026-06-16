package core.job;

public class JobRequestBuilder {

    public static String buildRequestBody(JobExecutionRequest request) {
        return """
                {
                  "jobType": "%s",
                  "jobName": "%s",
                  "parameters": "%s"
                }
                """.formatted(
                escapeJson(request.getJobType()),
                escapeJson(request.getJobName()),
                escapeJson(request.getParameters())
        );
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}