package iteration1;

import models.CreateUserRequest;
import org.junit.jupiter.api.Test;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class CreateAccountTest extends BaseTest {

    @Test
    public void userCanCreateAccountTest() {
        // create user
        var createUser = AdminSteps.createUser();
        CreateUserRequest userRequest = createUser.getRequest();

        // register user for further deletion
        registerCreatedUser(createUser.getResponse());

        // send create user request
        new CrudRequester(
                Endpoint.ACCOUNTS,
                RequestSpecs.authAsUser(
                        userRequest.getUsername(),
                        userRequest.getPassword()),
                ResponseSpecs.entityIsCreated())
                .post(null);

        // Next steps: need to request all user's accounts and check that the one is created among them
    }
}
