package core.job;

public class JobExecutionRequest {

    private final String jobType;
    private final String jobName;
    private final String parameters;

    public JobExecutionRequest(String jobType, String jobName, String parameters) {
        this.jobType = jobType;
        this.jobName = jobName;
        this.parameters = parameters;
    }

    public String getJobType() {
        return jobType;
    }

    public String getJobName() {
        return jobName;
    }

    public String getParameters() {
        return parameters;
    }
}