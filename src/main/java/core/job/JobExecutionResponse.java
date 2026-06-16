package core.job;

public class JobExecutionResponse {

    private final int httpStatusCode;
    private final String responseBody;
    private final int exitCode;

    public JobExecutionResponse(int httpStatusCode, String responseBody, int exitCode) {
        this.httpStatusCode = httpStatusCode;
        this.responseBody = responseBody;
        this.exitCode = exitCode;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public int getExitCode() {
        return exitCode;
    }
}