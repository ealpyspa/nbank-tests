package ui.iteration2;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import generators.RandomModelGenerator;
import models.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openqa.selenium.Alert;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.skeleton.requesters.ValidatedCrudeRequester;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.Map;

import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.Assertions.assertThat;

public class TransferMoneyTest {
    @BeforeAll
    public static void setupSelenoid() {
        Configuration.remote = "http://localhost:4444/wd/hub";
        Configuration.baseUrl = "http://192.168.1.67:3000";
        Configuration.browser = "chrome";
        Configuration.browserSize = "1920x1080";

        Configuration.browserCapabilities.setCapability("selenoid:options",
                Map.of("enableVNC", true, "enableLog", true));
    }

    @ParameterizedTest
    @CsvSource("1, 1")
    public void userCanMakeTransferToAccountTest(float balance, float amount) {
        CreateUserRequest user1 = AdminSteps.createUser().getRequest();

        CreateAccountResponse createAccountResponse1 = UserSteps.userCreatesAccount(user1);

        long createdAccountId = createAccountResponse1.getId();

        String createdAccountNumber = createAccountResponse1.getAccountNumber();

        DepositMoneyRequest depositMoneyRequest = DepositMoneyRequest.builder()
                .id(createdAccountId)
                .balance(balance)
                .build();

        UserSteps.depositMoneyResponse(user1, depositMoneyRequest);

        float afterDepositBalance = new ValidatedCrudeRequester<UserGetAccountsResponse>(
                Endpoint.CUSTOMER_ACCOUNTS,
                RequestSpecs.authAsUser(user1.getUsername(), user1.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .getAll()
                .get(0).getBalance();

        CreateUserRequest user2 = AdminSteps.createUser().getRequest();

        CreateAccountResponse createAccountResponse2 = UserSteps.userCreatesAccount(user2);

        String createdAccount2Number = createAccountResponse2.getAccountNumber();

        float beforeTransferBalance = createAccountResponse2.getBalance();

        String userAuthHeader = new CrudRequester(
                Endpoint.LOGIN,
                RequestSpecs.unauthSpec(),
                ResponseSpecs.requestReturnsOk())
                .post(LoginUserRequest.builder().username(user1.getUsername()).password(user1.getPassword()).build())
                .extract()
                .header("Authorization");

        open("/");

        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);

        open("/dashboard");

        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();

        assertThat($(Selectors.byText("\uD83D\uDD04 Make a Transfer")).isDisplayed()).isTrue();

        $(Selectors.byText("-- Choose an account --")).click();

        $(Selectors.byText(createdAccountNumber)).click();

        $(Selectors.byAttribute("placeholder","Enter recipient name")).sendKeys(user2.getUsername());

        $(Selectors.byAttribute("placeholder","Enter recipient account number")).sendKeys(createdAccount2Number);

        String amountText = String.valueOf(amount);

        $(Selectors.byAttribute("placeholder","Enter amount")).sendKeys(amountText);

        $(Selectors.byId("confirmCheck")).click();

        $(Selectors.byText("\uD83D\uDE80 Send Transfer")).click();

        Alert alert = switchTo().alert();

        assertThat(alert.getText()).matches("✅ Successfully transferred \\$" + amount + " to account " + createdAccount2Number + "!");

        alert.accept();

        float afterTransferBalance1 = new ValidatedCrudeRequester<UserGetAccountsResponse>(
                Endpoint.CUSTOMER_ACCOUNTS,
                RequestSpecs.authAsUser(user1.getUsername(), user1.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .getAll()
                .get(0).getBalance();

        assertThat(afterTransferBalance1).isEqualTo(afterDepositBalance - amount);

        float afterTransferBalance2 = new ValidatedCrudeRequester<UserGetAccountsResponse>(
                Endpoint.CUSTOMER_ACCOUNTS,
                RequestSpecs.authAsUser(user2.getUsername(), user2.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .getAll()
                .get(0).getBalance();

        assertThat(afterTransferBalance2).isEqualTo(beforeTransferBalance + amount);
    }

    @ParameterizedTest
    @CsvSource("1, 1")
    public void userCannotMakeTransferToNotExistingAccount(float amount, float balance) {
        CreateUserRequest user1 = AdminSteps.createUser().getRequest();

        CreateAccountResponse createAccountResponse1 = UserSteps.userCreatesAccount(user1);

        long createdAccountId = createAccountResponse1.getId();

        String createdAccountNumber = createAccountResponse1.getAccountNumber();

        DepositMoneyRequest depositMoneyRequest = DepositMoneyRequest.builder()
                .id(createdAccountId)
                .balance(balance)
                .build();

        UserSteps.depositMoneyResponse(user1, depositMoneyRequest);

        float afterDepositBalance = new ValidatedCrudeRequester<UserGetAccountsResponse>(
                Endpoint.CUSTOMER_ACCOUNTS,
                RequestSpecs.authAsUser(user1.getUsername(), user1.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .getAll()
                .get(0).getBalance();

        String userAuthHeader = new CrudRequester(
                Endpoint.LOGIN,
                RequestSpecs.unauthSpec(),
                ResponseSpecs.requestReturnsOk())
                .post(LoginUserRequest.builder().username(user1.getUsername()).password(user1.getPassword()).build())
                .extract()
                .header("Authorization");

        open("/");

        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);

        open("/dashboard");

        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();

        assertThat($(Selectors.byText("\uD83D\uDD04 Make a Transfer")).isDisplayed()).isTrue();

        $(Selectors.byText("-- Choose an account --")).click();

        $(Selectors.byText(createdAccountNumber)).click();

        String randomUsernameAndAccountNumber= RandomModelGenerator.generate(CreateUserRequest.class).getUsername(); // same value for randomUserAccount

        $(Selectors.byAttribute("placeholder","Enter recipient name")).sendKeys(randomUsernameAndAccountNumber);

        $(Selectors.byAttribute("placeholder","Enter recipient account number")).sendKeys(randomUsernameAndAccountNumber);

        String amountText = String.valueOf(amount);

        $(Selectors.byAttribute("placeholder","Enter amount")).sendKeys(amountText);

        $(Selectors.byId("confirmCheck")).click();

        $(Selectors.byText("\uD83D\uDE80 Send Transfer")).click();

        Alert alert = switchTo().alert();

        assertThat(alert.getText()).contains("❌ No user found with this account number.");

        alert.accept();

        float afterTransferBalance1 = new ValidatedCrudeRequester<UserGetAccountsResponse>(
                Endpoint.CUSTOMER_ACCOUNTS,
                RequestSpecs.authAsUser(user1.getUsername(), user1.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .getAll()
                .get(0).getBalance();

        assertThat(afterTransferBalance1).isEqualTo(afterDepositBalance);
    }
}
