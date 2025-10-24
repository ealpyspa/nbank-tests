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
        var createUser = AdminSteps.createUser();
        CreateUserRequest userRequest = createUser.getRequest();

        new CrudRequester(
                Endpoint.ACCOUNTS,
                RequestSpecs.authAsUser(
                        userRequest.getUsername(),
                        userRequest.getPassword()),
                ResponseSpecs.entityIsCreated())
                .post(null);

        // Next steps: need to request all user's accounts and check that the one is created among them

        // delete user by admin
        long userId = createUser.getResponse().getId();
        AdminSteps.deleteUser(userId);
    }
}
