package stepdefinitions.job;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import stepdefinitions.BaseSteps;

public class JobSteps extends BaseSteps {

    @When("job's endpoint is available$")
    public void jobs_endpoint_is_available() {
        jobActions().jobEndpointIsAvailable();
    }

    @When("run (.+) (.+) with parameters (.+)$")
    public void run_job_with_parameters(
            String jobType,
            String jobName,
            String parameters
    ) {
        jobActions().runJobWithParameters(jobType, jobName, parameters);
    }

    @Then("its exit code is 0")
    public void its_exit_code_is() {
        jobActions().assertExitCodeEquals();
    }
}