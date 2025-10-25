package api.iteration2;

import api.iteration1.BaseTest;
import models.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.ValidatedCrudeRequester;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.stream.Stream;

public class DepositMoneyTest extends BaseTest {

    public static Stream<Arguments> validAmountOfMoney() {
        return Stream.of(
                // minimal allowed amount of money
                Arguments.of(Float.MIN_NORMAL),
                // maximum allowed amount of money
                Arguments.of(5000)
        );
    }

    // Positive test: Authorized user can top up their account with valid sum
    // Here is an issue: sometimes actual result is 1.1754944E-38 (when equalTo(balance) is used),
    // another time 1.1754944E-38F (when equalTo(1.1754944E-38) is hardcoded)
    // casting to double doesn't help
    // Question: can it be fixed in API, so it returns same result constantly?
    // Workaround solution: extract value and use assertEquals()
    @ParameterizedTest
    @MethodSource("validAmountOfMoney")
    public void userCanTopUpAccount(float balance) {
        // create user
        var createdUser = AdminSteps.createUser();
        CreateUserRequest createUserRequest = createdUser.getRequest();

        // register user for further deletion
        registerCreatedUser(createdUser.getResponse());

        // create account
        CreateAccountResponse createAccountResponse = UserSteps.userCreatesAccount(createUserRequest);

        // extract id of created account
        long createdAccountId = createAccountResponse.getId();

        // extracts initial balance of created account
        float createdAccountBalance = createAccountResponse.getBalance();

        // create a request to make a deposit (leave as is because of parameters)
        DepositMoneyRequest depositMoneyRequest = DepositMoneyRequest.builder()
                .id(createdAccountId)
                .balance(balance)
                .build();

        // make a deposit + get amount of it + compare
        float actualAmount = new ValidatedCrudeRequester<DepositMoneyResponse>(
                Endpoint.ACCOUNTS_DEPOSIT,
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .post(depositMoneyRequest)
                .getTransactions().getFirst().getAmount();

        softly.assertThat(actualAmount).isEqualTo(balance);
        //ModelAssertions.assertThatModels(depositMoneyRequest, depositMoneyResponse).match(); ->
        // rework so this work -> model-comparison.properties: DepositMoneyRequest=DepositMoneyResponse:balance=transactions[0].amount

        // check balance is changed and equal to amount of deposit
        float actualBalance = new ValidatedCrudeRequester<UserGetAccountsResponse>(
                Endpoint.CUSTOMER_ACCOUNTS,
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .getAll().get(0).getBalance();

        softly.assertThat(actualBalance).isEqualTo(createdAccountBalance + balance);

    }
}
