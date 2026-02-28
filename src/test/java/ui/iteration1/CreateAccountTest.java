package ui.iteration1;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import models.CreateAccountResponse;
import models.CreateUserRequest;
import models.LoginUserRequest;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.codeborne.selenide.Selenide.*;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class CreateAccountTest {
    @BeforeAll
    public static void setupSelenoid() {
        Configuration.remote = "http://localhost:4444/wd/hub";
        Configuration.baseUrl = "http://192.168.1.67:3000";
        Configuration.browser = "chrome";
        Configuration.browserSize = "1920x1080";

        Configuration.browserCapabilities.setCapability("selenoid:options",
                Map.of("enableVNC", true, "enableLog", true));
    }

    @Test
    public void userCanCreateAccountTest() {

        // Preconditions
        // Step 1: admin login
        // Step 2: admin creates user
        // Step 3: user login
        CreateUserRequest user = AdminSteps.createUser().getRequest();

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

        // Test Steps
        // Step 4: user create account

        $(Selectors.byText("➕ Create New Account")).click();

        // Step 5: check successful alert that account is created on UI

        Alert alert = switchTo().alert();

        String alertText = alert.getText();

        assertThat(alertText.contains("✅ New Account Created! Account Number:"));

        alert.accept();

        Pattern pattern = Pattern.compile("Account Number: (\\w+)");

        Matcher matcher = pattern.matcher(alertText);

        matcher.find();

        String createdAccountNumber = matcher.group(1);

        // Step 6: account is created in API

        CreateAccountResponse[] existingUserAccounts = given()
                .spec(RequestSpecs.authAsUser(user.getUsername(), user.getPassword()))
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then().assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract().as(CreateAccountResponse[].class);

        assertThat(existingUserAccounts).hasSize(1);

        CreateAccountResponse createdAccount = existingUserAccounts[0];

        assertThat(createdAccount).isNotNull();
        assertThat(createdAccount.getBalance()).isZero();
    }
}
