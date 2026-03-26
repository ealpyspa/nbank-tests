package ui.iteration1;

import api.models.CreateUserRequest;
import api.requests.steps.AdminSteps;
import com.codeborne.selenide.Condition;
import common.annotations.Browsers;
import org.junit.jupiter.api.Test;
import ui.BaseUiTest;
import ui.pages.AdminPanel;
import ui.pages.BankAlert;
import ui.pages.LoginPage;
import ui.pages.UserDashboard;

import static com.codeborne.selenide.Selenide.executeJavaScript;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class LoginUserTest extends BaseUiTest {
    @Test
    @Browsers({"chrome"})
    public void adminCanLoginWithCorrectDataTest() {
        CreateUserRequest admin = CreateUserRequest.getAdmin();

        new LoginPage().open().login(admin.getUsername(), admin.getPassword())
                        .getPage(AdminPanel.class).getAdminPanelText().shouldBe(Condition.visible);

        String token = executeJavaScript("return window.localStorage.getItem('authToken');" );

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    @Browsers({"chrome"})
    public void userCanLoginWithCorrectDataTest() {
        CreateUserRequest user = AdminSteps.createUser().getRequest();

        new LoginPage().open().login(user.getUsername(), user.getPassword())
                .getPage(UserDashboard.class).getWelcomeText().shouldBe(Condition.visible).shouldHave(Condition.text("Welcome, noname!"));

        String token = executeJavaScript("return window.localStorage.getItem('authToken');");

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();;
    }

    @Test
    @Browsers({"chrome"})
    public void userCannotLoginWithIncorrectDataTest() {
        CreateUserRequest user = AdminSteps.createUser().getRequest();

        user.setUsername("a");

        new LoginPage().open().login(user.getUsername(), user.getPassword())
                .checkAlertAndAccept(BankAlert.INVALID_CREDENTIALS.getMessage());

        String token = executeJavaScript("return window.localStorage.getItem('authToken');");

        assertThat(token).isNull();
    }
}
