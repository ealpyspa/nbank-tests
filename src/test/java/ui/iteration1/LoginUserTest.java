package ui.iteration1;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import models.CreateUserRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import requests.steps.AdminSteps;

import java.util.Map;

import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class LoginUserTest {
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
    public void adminCanLoginWithCorrectDataTest() {
        CreateUserRequest admin = CreateUserRequest.builder().username("admin").password("admin").build();

        open("/login");

        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(admin.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(admin.getPassword());
        $("button").click();

        $(Selectors.byText("Admin Panel")).shouldBe(Condition.visible);

        String token = executeJavaScript("return window.localStorage.getItem('authToken');" );

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    public void userCanLoginWithCorrectDataTest() {
        // create user via API
        CreateUserRequest user = AdminSteps.createUser().getRequest();

        open("/login");

        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(user.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(user.getPassword());
        $("button").click();

        $(Selectors.byClassName("welcome-text")).shouldBe(Condition.visible).shouldHave(Condition.text("Welcome, noname!"));

        String token = executeJavaScript("return window.localStorage.getItem('authToken');");

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();;
    }

    @Test
    public void userCannotLoginWithIncorrectDataTest() {
        CreateUserRequest user = AdminSteps.createUser().getRequest();

        user.setUsername("a");

        open("/login");

        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(user.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(user.getPassword());
        $("button").click();

        Alert alert = switchTo().alert();

        String alertText = alert.getText();

        assertThat(alertText.contains("Invalid credentialsAxiosError: Request failed with status code 401"));

        alert.accept();

        String token = executeJavaScript("return window.localStorage.getItem('authToken');");

        assertThat(token).isNull();
    }
}
