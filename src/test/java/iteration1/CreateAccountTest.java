package iteration1;

import generators.RandomData;
import models.CreateUserRequest;
import models.UserRole;
import org.junit.jupiter.api.Test;
import requests.AdminCreateUserRequester;
import requests.CreateAccountRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class CreateAccountTest {

    @Test
    public void userCanCreateAccountTest() {
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getUserPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityIsCreated())
                .post(userRequest);

        new CreateAccountRequester(
                RequestSpecs.authAsUser(
                        userRequest.getUsername(),
                        userRequest.getPassword()),
                ResponseSpecs.entityIsCreated())
                .post(null);

        // Next steps: need to request all user's accounts and check that the one is created among them
    }
}
