package ui.iteration1;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selectors;
import generators.RandomModelGenerator;
import models.CreateUserRequest;
import models.CreateUserResponse;
import models.comparison.ModelAssertions;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import specs.RequestSpecs;

import java.util.Arrays;
import java.util.Map;

import static com.codeborne.selenide.Selenide.*;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CreateUserTest {
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
    public void adminCanCreateUserTest() {
        // Step 1: Admin is created and login (but better via API) =========> think
        CreateUserRequest admin = CreateUserRequest.builder().username("admin").password("admin").build();

        open("/login");

        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(admin.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(admin.getPassword());
        $("button").click();

        $(Selectors.byText("Admin Panel")).shouldBe(Condition.visible);

        // Step 2: Admin creates user
        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);

        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(newUser.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(newUser.getPassword());
        $(Selectors.byText("Add User")).click();

        // Step 3: Alert "✅ User created successfully!" is displayed
        Alert alert = switchTo().alert();

        assertEquals(alert.getText(), "✅ User created successfully!");

        alert.accept();

        // Step 4: New user is displayed on UI
        ElementsCollection allUserFromDashboard = $(Selectors.byText("All Users")).parent().findAll("li");
        allUserFromDashboard.findBy(Condition.exactText(newUser.getUsername() + "\nUSER")).shouldBe(Condition.visible);

        // Step 5: New user is created on API
        CreateUserResponse[] users = given()
                .spec(RequestSpecs.adminSpec())
                .get("http://localhost:4111/api/v1/admin/users")
                .then().assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract().as(CreateUserResponse[].class);

        CreateUserResponse createdUSer = Arrays.stream(users)
                .filter(user -> user.getUsername().equals(newUser.getUsername()))
                .findFirst().get();

        ModelAssertions.assertThatModels(newUser, createdUSer).match();
    }

    @Test
    public void adminCannotCreateUserWithInvalidDataTest() {
        // Step 1: Admin is created and login (but better via API) =========> think
        CreateUserRequest admin = CreateUserRequest.builder().username("admin").password("admin").build();

        open("/login");

        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(admin.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(admin.getPassword());
        $("button").click();

        $(Selectors.byText("Admin Panel")).shouldBe(Condition.visible);

        // Step 2: Admin creates user
        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);
        newUser.setUsername("a");

        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(newUser.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(newUser.getPassword());
        $(Selectors.byText("Add User")).click();

        // Step 3: Alert with text "Username must be between 3 and 15 characters" is displayed
        Alert alert = switchTo().alert();

        assertThat(alert.getText()).contains("Username must be between 3 and 15 characters");

        alert.accept();

        // Step 4: New user is NOT displayed on UI
        ElementsCollection allUserFromDashboard = $(Selectors.byText("All Users")).parent().findAll("li");
        allUserFromDashboard.findBy(Condition.exactText(newUser.getUsername() + "\nUSER")).shouldNotBe(Condition.exist);

        // Step 5: New user is NOT created on API
        CreateUserResponse[] users = given()
                .spec(RequestSpecs.adminSpec())
                .get("http://localhost:4111/api/v1/admin/users")
                .then().assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract().as(CreateUserResponse[].class);

        long usersWithSameUserNameAsNewUSer = Arrays.stream(users)
                .filter(user -> user.getUsername().equals(newUser.getUsername()))
                .count();

        assertThat(usersWithSameUserNameAsNewUSer).isZero();
    }
}
