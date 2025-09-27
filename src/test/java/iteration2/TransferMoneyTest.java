package iteration2;

import generators.RandomData;
import io.restassured.common.mapper.TypeRef;
import iteration1.BaseTest;
import models.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.*;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.List;
import java.util.stream.Stream;

public class TransferMoneyTest extends BaseTest {
    public static Stream<Arguments> validAmountOfMoney() {
        return Stream.of(
                // minimal sum of transfer
                Arguments.of(Float.MIN_NORMAL, Float.MIN_NORMAL, "Transfer successful"),
                // maximum sum of transfer
                Arguments.of(10000 + Float.MIN_NORMAL, 10000, "Transfer successful")
        );
    }

    @ParameterizedTest
    @MethodSource("validAmountOfMoney")
    // Positive test: User can transfer valid amount of money to another user's account
    public void useCanTransferMoneyToAnotherUserTest(float balance, float amount, String message) {
        // prepare request for user1 creation
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getUserPassword())
                .role(UserRole.USER.toString())
                .build();

        // create user1
        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityIsCreated())
                .post(createUserRequest);

        // user 1 creates account + extract id
        long createdAccountId = new CreateAccountRequester(
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.entityIsCreated())
                .post(null)
                .extract()
                .as(CreateAccountResponse.class).getId();

        // create a request to make a deposit
        DepositMoneyRequest depositMoneyRequest = DepositMoneyRequest.builder()
                .id(createdAccountId)
                .balance(balance)
                .build();

        // user1 top up their account with money (enough to make transfer)
        // as maximum sum transfer check -> do top up twice
        // 1st top up
        new UserDepositMoneyRequester(
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .post(depositMoneyRequest);

        // 2nd top up
        new UserDepositMoneyRequester(
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .post(depositMoneyRequest);

        // prepare request to create user2
        CreateUserRequest createUser2Request = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getUserPassword())
                .role(UserRole.USER.toString())
                .build();

        // create user2
        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityIsCreated())
                .post(createUser2Request);

        // user 2 creates account + extract id
        long createdAccount2Id = new CreateAccountRequester(
                RequestSpecs.authAsUser(createUser2Request.getUsername(), createUser2Request.getPassword()),
                ResponseSpecs.entityIsCreated())
                .post(null)
                .extract()
                .as(CreateAccountResponse.class).getId();

        // prepare transfer money request
        TransferMoneyRequest transferMoneyRequest = TransferMoneyRequest.builder()
                .senderAccountId(createdAccountId)
                .receiverAccountId(createdAccount2Id)
                .amount(amount)
                .build();

        // user1 transfers valid amount of money to user2 account
        TransferMoneyResponse transferMoneyResponse = new TransferMoneyRequester(
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .post(transferMoneyRequest)
                .extract()
                .as(TransferMoneyResponse.class);

        float actualAmount = transferMoneyResponse.getAmount();
        String actualMessage = transferMoneyResponse.getMessage();

        softly.assertThat(actualAmount).isEqualTo(amount);
        softly.assertThat(actualMessage).isEqualTo(message);

        // check that user1 account balance has changed
        List<UserGetAccountsResponse> userGetAccountsResponseList = new UserGetsAccountsRequester(
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .post(null)
                .extract()
                .as(new TypeRef<List<UserGetAccountsResponse>>() {
                });

        float actualBalance = userGetAccountsResponseList.get(0).getBalance();
        softly.assertThat(actualBalance).isEqualTo(balance);

    }

}
