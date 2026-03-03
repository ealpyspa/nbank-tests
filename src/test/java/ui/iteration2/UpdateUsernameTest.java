package ui.iteration2;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import api.generators.RandomModelGenerator;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.models.GetCustomerProfileResponse;
import api.models.LoginUserRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openqa.selenium.Alert;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.CrudRequester;
import api.requests.skeleton.requesters.ValidatedCrudeRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import java.util.Map;

import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.Assertions.assertThat;

public class UpdateUsernameTest {
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
    @ValueSource(strings = "Test Testov")
    public void userCanUpdateNameToValidTest(String newName) {
        // CreateUserRequest createUserRequest = AdminSteps.createUser(); -> not applicable as need to extract "name"
        CreateUserRequest user = RandomModelGenerator.generate(CreateUserRequest.class);

        // send user create request + extract name (expected: name=null) + for further extract userId for delete
        CreateUserResponse createUserResponse = new ValidatedCrudeRequester<CreateUserResponse>(
                Endpoint.ADMIN_USER,
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityIsCreated())
                .post(user);

        String initialName = createUserResponse.getName();

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

        $(".user-username").click();

        assertThat($(Selectors.byText("✏\uFE0F Edit Profile")).isDisplayed()).isTrue();

        $(Selectors.byAttribute("placeholder", "Enter new name")).sendKeys(newName);

        $(Selectors.byText("\uD83D\uDCBE Save Changes")).click();

        Alert alert = switchTo().alert();

        assertThat(alert.getText()).contains("✅ Name updated successfully!");

        alert.accept();

        refresh();

        $(Selectors.byText("✏\uFE0F Edit Profile")).shouldBe(Condition.visible);

        SelenideElement userName = $(".user-name");

        userName.shouldBe(Condition.visible);

        assertThat(userName.getText()).isEqualTo(newName);

        String updatedName = new ValidatedCrudeRequester<GetCustomerProfileResponse>(
                Endpoint.CUSTOMER_PROFILE_GET,
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .getAll()
                .get(0).getName();

        assertThat(updatedName).isEqualTo(newName);
        assertThat(updatedName).isNotEqualTo(initialName);
    }

    @ParameterizedTest
    @ValueSource(strings = "a")
    public void userCannotUpdateNameToInvalidTest(String newName) {
        // CreateUserRequest createUserRequest = AdminSteps.createUser(); -> not applicable as need to extract "name"
        CreateUserRequest user = RandomModelGenerator.generate(CreateUserRequest.class);

        // send user create request + extract name (expected: name=null) + for further extract userId for delete
        CreateUserResponse createUserResponse = new ValidatedCrudeRequester<CreateUserResponse>(
                Endpoint.ADMIN_USER,
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityIsCreated())
                .post(user);

        String initialName = createUserResponse.getName();

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

        $(".user-username").click();

        assertThat($(Selectors.byText("✏\uFE0F Edit Profile")).isDisplayed()).isTrue();

        $(Selectors.byAttribute("placeholder", "Enter new name")).sendKeys(newName);

        $(Selectors.byText("\uD83D\uDCBE Save Changes")).click();

        Alert alert = switchTo().alert();

        assertThat(alert.getText()).contains("❌ Please enter a valid name.");

        alert.accept();

        String actualName = new ValidatedCrudeRequester<GetCustomerProfileResponse>(
                Endpoint.CUSTOMER_PROFILE_GET,
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .getAll()
                .get(0).getName();

        assertThat(actualName).isEqualTo(initialName);
    }
}
