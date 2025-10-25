package requests.steps;

import io.restassured.response.ValidatableResponse;
import models.CreateAccountResponse;
import models.CreateUserRequest;
import models.DepositMoneyRequest;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.skeleton.requesters.ValidatedCrudeRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class UserSteps {
    public static CreateAccountResponse userCreatesAccount(CreateUserRequest createUserRequest) {
        return new ValidatedCrudeRequester<CreateAccountResponse>(
                Endpoint.ACCOUNTS,
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.entityIsCreated())
                .post(null);
    }

    public static ValidatableResponse depositMoneyResponse (CreateUserRequest createUserRequest, DepositMoneyRequest depositMoneyRequest) {
         return new CrudRequester(
                Endpoint.ACCOUNTS_DEPOSIT,
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .post(depositMoneyRequest);
    }
}
