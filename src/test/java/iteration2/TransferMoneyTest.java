package iteration2;

import iteration1.BaseTest;
import models.*;
import models.comparison.ModelAssertions;
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

public class TransferMoneyTest extends BaseTest {
    public static Stream<Arguments> validAmountOfMoney() {
        return Stream.of(
                // minimal sum of transfer
                Arguments.of(Float.MIN_NORMAL, Float.MIN_NORMAL, "Transfer successful"),
                // maximum sum of transfer
                Arguments.of(5000 + Float.MIN_NORMAL, 5000, "Transfer successful")
        );
    }

    @ParameterizedTest
    @MethodSource("validAmountOfMoney")
    // Positive test: User can transfer valid amount of money to another user's account
    public void useCanTransferMoneyToAnotherUserTest(float balance, float amount, String message) {
        // create user1
        var createUser1 = AdminSteps.createUser();
        CreateUserRequest createUserRequest = createUser1.getRequest();

        // user 1 creates account
        long createdAccountId = UserSteps.userCreatesAccount(createUserRequest).getId();

        // create a request to make a deposit (leave as is because of parameters)
        DepositMoneyRequest depositMoneyRequest = DepositMoneyRequest.builder()
                .id(createdAccountId)
                .balance(balance)
                .build();

        // user1 top up their account with money (enough to make transfer)
        // as maximum sum transfer check -> do top up twice
        // 1st top up
        UserSteps.depositMoneyResponse(createUserRequest, depositMoneyRequest);

        // 2nd top up
        UserSteps.depositMoneyResponse(createUserRequest, depositMoneyRequest);

        // get balance after 2 deposits
        float afterDepositBalance = new ValidatedCrudeRequester<UserGetAccountsResponse>(
                Endpoint.CUSTOMER_ACCOUNTS,
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .getAll()
                .get(0).getBalance();

        // create user2
        var createUser2 = AdminSteps.createUser();
        CreateUserRequest createUser2Request = createUser2.getRequest();

        // user 2 creates account + extract id
        long createdAccount2Id = UserSteps.userCreatesAccount(createUser2Request).getId();

        // prepare transfer money request (leave as is because of parameters)
        TransferMoneyRequest transferMoneyRequest = TransferMoneyRequest.builder()
                .senderAccountId(createdAccountId)
                .receiverAccountId(createdAccount2Id)
                .amount(amount)
                .build();

        // user1 transfers valid amount of money to user2 account
        TransferMoneyResponse transferMoneyResponse = new ValidatedCrudeRequester<TransferMoneyResponse>(
                Endpoint.ACCOUNTS_TRANSFER,
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .post(transferMoneyRequest);

        String actualMessage = transferMoneyResponse.getMessage();

        ModelAssertions.assertThatModels(transferMoneyRequest, transferMoneyResponse).match(); //amount
        softly.assertThat(actualMessage).isEqualTo(message);

        // check that user1 account balance has changed
        float actualBalance = new ValidatedCrudeRequester<UserGetAccountsResponse>(
                Endpoint.CUSTOMER_ACCOUNTS,
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .getAll()
                .get(0).getBalance();

        softly.assertThat(actualBalance).isEqualTo(afterDepositBalance - amount);

        // delete user1 by admin
        long user1Id = createUser1.getResponse().getId();
        AdminSteps.deleteUser(user1Id);

        // delete user2 by admin
        long user2Id = createUser2.getResponse().getId();
        AdminSteps.deleteUser(user2Id);

    }

}
