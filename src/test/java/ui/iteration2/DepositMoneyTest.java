package ui.iteration2;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import api.models.LoginUserRequest;
import api.models.UserGetAccountsResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openqa.selenium.Alert;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.CrudRequester;
import api.requests.skeleton.requesters.ValidatedCrudeRequester;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import java.util.Map;

import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.Assertions.assertThat;

public class DepositMoneyTest {
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
    @CsvSource("1")
    public void userCanDepositMoneyWithValidSumTest(float amount) {
        // Preconditions
        // Step 1: admin login
        // Step 2: admin creates user
        // Step 3: user creates account
        // Step 4: user login
        CreateUserRequest user = AdminSteps.createUser().getRequest();

        CreateAccountResponse createAccountResponse = UserSteps.userCreatesAccount(user);

        float initialBalance = createAccountResponse.getBalance();

        long accountId = createAccountResponse.getId();

        String userAuthHeader = new CrudRequester(
                Endpoint.LOGIN,
                RequestSpecs.unauthSpec(),
                ResponseSpecs.requestReturnsOk())
                .post(LoginUserRequest.builder().username(user.getUsername()).password(user.getPassword()).build())
                .extract()
                .header("Authorization");

        open("/");

        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);

        open("/dashboard");

        // Test steps
        // Step 5: makes deposit with valid sum
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).click();

        assertThat($(Selectors.byText("\uD83D\uDCB0 Deposit Money")).isDisplayed()).isTrue();

        $(Selectors.byText("-- Choose an account --")).click();

        String createdAccountNumber = createAccountResponse.getAccountNumber();

        $(Selectors.byText(createdAccountNumber)).click();

        String amountText = String.valueOf(amount);

        $(Selectors.byAttribute("placeholder","Enter amount")).sendKeys(amountText);

        $(Selectors.byText("\uD83D\uDCB5 Deposit")).click();

        Alert alert = switchTo().alert();

        assertThat(alert.getText()).matches("✅ Successfully deposited \\$" + amount + " to account \\S+!");

        alert.accept();

        float actualBalance = new ValidatedCrudeRequester<CreateAccountResponse>(
                Endpoint.CUSTOMER_ACCOUNTS,
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .getAll().get(0).getBalance();

        assertThat(actualBalance).isEqualTo(initialBalance + 1);

        // user logins twice: 1) during account creation and 2) in order to get authToken -> how to optimize
    }

    @ParameterizedTest
    @CsvSource("0")
    public void userCannotMakeDepositWithInvalidSumTest(float amount) {
        // Preconditions
        // Step 1: admin login
        // Step 2: admin creates user
        // Step 3: user creates account
        // Step 4: user login
        CreateUserRequest user = AdminSteps.createUser().getRequest();

        CreateAccountResponse createAccountResponse = UserSteps.userCreatesAccount(user);

        float initialBalance = createAccountResponse.getBalance();

        long accountId = createAccountResponse.getId();

        String userAuthHeader = new CrudRequester(
                Endpoint.LOGIN,
                RequestSpecs.unauthSpec(),
                ResponseSpecs.requestReturnsOk())
                .post(LoginUserRequest.builder().username(user.getUsername()).password(user.getPassword()).build())
                .extract()
                .header("Authorization");

        open("/");

        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);

        open("/dashboard");

        // Test steps
        // Step 5: makes deposit with invalid sum
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).click();

        assertThat($(Selectors.byText("\uD83D\uDCB0 Deposit Money")).isDisplayed()).isTrue();

        $(Selectors.byText("-- Choose an account --")).click();

        String createdAccountNumber = createAccountResponse.getAccountNumber();

        $(Selectors.byText(createdAccountNumber)).click();

        String amountText = String.valueOf(amount);

        $(Selectors.byAttribute("placeholder","Enter amount")).sendKeys(amountText);

        $(Selectors.byText("\uD83D\uDCB5 Deposit")).click();

        Alert alert = switchTo().alert();

        assertThat(alert.getText()).contains("❌ Please enter a valid amount.");

        alert.accept();

        float actualBalance = new ValidatedCrudeRequester<UserGetAccountsResponse>(
                Endpoint.CUSTOMER_ACCOUNTS,
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .getAll().get(0).getBalance();

        assertThat(actualBalance).isEqualTo(initialBalance);
    }
}
