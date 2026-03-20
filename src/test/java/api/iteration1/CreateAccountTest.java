package api.iteration1;

import api.models.CreateUserRequest;
import org.junit.jupiter.api.Test;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.CrudRequester;
import api.requests.steps.AdminSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

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
