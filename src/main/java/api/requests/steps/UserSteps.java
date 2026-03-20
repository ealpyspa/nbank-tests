package api.requests.steps;

import api.models.*;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.CrudRequester;
import api.requests.skeleton.requesters.ValidatedCrudeRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import common.helper.StepLogger;
import io.restassured.response.ValidatableResponse;

import java.util.List;

public class UserSteps {
    private String username;
    private String password;

    public UserSteps(String username, String password) {
        this.password = password;
        this.username = username;
    }

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

    public List<UserGetAccountsResponse> getAllAccounts() {
        return new ValidatedCrudeRequester<UserGetAccountsResponse>(
                Endpoint.CUSTOMER_ACCOUNTS,
                RequestSpecs.authAsUser(username, password),
                ResponseSpecs.requestReturnsOk()).getAll(UserGetAccountsResponse[].class);
    }

    public GetCustomerProfileResponse getCustomerProfile() {
        return new ValidatedCrudeRequester<GetCustomerProfileResponse>(
                Endpoint.CUSTOMER_PROFILE_GET,
                RequestSpecs.authAsUser(username, password),
                ResponseSpecs.requestReturnsOk())
                .getOne();
    }
}
