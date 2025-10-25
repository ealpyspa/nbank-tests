package iteration1;

import models.CreateUserRequest;
import models.CreateUserResponse;
import models.LoginUserRequest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.skeleton.requesters.ValidatedCrudeRequester;
import requests.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class LoginUserTest extends BaseTest{

    @Test
    public void adminCanGenerateAuthTokenTest() {
        LoginUserRequest loginUserRequest = LoginUserRequest.builder()
                .username("admin")
                .password("admin")
                .build();

        new ValidatedCrudeRequester<CreateUserResponse>(
                Endpoint.LOGIN,
                RequestSpecs.unauthSpec(),
                ResponseSpecs.requestReturnsOk())
                .post(loginUserRequest);

    }

    @Test
    public void userCanGenerateAuthTokenTest() {
        var createUser = AdminSteps.createUser();
        CreateUserRequest userRequest = createUser.getRequest();

        // register user for further deletion
        registerCreatedUser(createUser.getResponse());

        new CrudRequester(
                Endpoint.LOGIN,
                RequestSpecs.unauthSpec(),
                ResponseSpecs.requestReturnsOk())
                .post(LoginUserRequest.builder()
                        .username(userRequest.getUsername())
                        .password(userRequest.getPassword())
                        .build())
                .header("Authorization", Matchers.notNullValue());

    }
}
