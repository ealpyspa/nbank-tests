package api.iteration2;

import generators.RandomData;
import io.restassured.common.mapper.TypeRef;
import api.iteration1.BaseTest;
import models.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.AdminCreateUserRequester;
import requests.CreateAccountRequester;
import requests.UserDepositMoneyRequester;
import requests.UserGetsAccountsRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.List;
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
        // prepare request for user creation
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getUserPassword())
                .role(UserRole.USER.toString())
                .build();

        // create user
        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityIsCreated())
                .post(createUserRequest);

        // create account
        CreateAccountResponse createAccountResponse = new CreateAccountRequester(
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.entityIsCreated())
                .post(null)
                .extract()
                .as(CreateAccountResponse.class);

        // extract id of created account
        long createdAccountId = createAccountResponse.getId();

        // extracts initial balance of created account
        float createdAccountBalance = createAccountResponse.getBalance();

        // create a request to make a deposit
        DepositMoneyRequest depositMoneyRequest = DepositMoneyRequest.builder()
                .id(createdAccountId)
                .balance(balance)
                .build();

        // make a deposit + get amount of it + compare
        float actualAmount = new UserDepositMoneyRequester(
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .post(depositMoneyRequest)
                .extract()
                .as(DepositMoneyResponse.class)
                .getTransactions().getFirst().getAmount();

        softly.assertThat(actualAmount).isEqualTo(balance);

        // check balance is changed and equal to amount of deposit
        List<UserGetAccountsResponse> accountsResponseList = new UserGetsAccountsRequester(
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .post(null)
                .extract()
                .as(new TypeRef<List<UserGetAccountsResponse>>(){});

        float actualBalance = accountsResponseList.get(0).getBalance();

        softly.assertThat(actualBalance).isEqualTo(createdAccountBalance + balance);
    }
}
