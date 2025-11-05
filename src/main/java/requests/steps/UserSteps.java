package requests.steps;

import common.helper.StepLogger;
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
        return StepLogger.log("User " + createUserRequest.getUsername() + " creates their account",
                () -> new ValidatedCrudeRequester<CreateAccountResponse>(
                    Endpoint.ACCOUNTS,
                    RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                    ResponseSpecs.entityIsCreated())
                    .post(null)
        );

    }

    public static ValidatableResponse depositMoneyResponse (CreateUserRequest createUserRequest, DepositMoneyRequest depositMoneyRequest) {
         return StepLogger.log("User " + createUserRequest.getUsername() + " deposits money to thei account",
                 () -> new CrudRequester(
                Endpoint.ACCOUNTS_DEPOSIT,
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .post(depositMoneyRequest)
         );
    }
}
