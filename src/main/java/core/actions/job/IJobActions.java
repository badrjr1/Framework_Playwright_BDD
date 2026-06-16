package core.actions.job;

public interface IJobActions {

    void jobEndpointIsAvailable();

    void runJobWithParameters(String jobType, String jobName, String parameters);

    void assertExitCodeEquals();
}