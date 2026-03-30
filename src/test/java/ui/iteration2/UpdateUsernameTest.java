package ui.iteration2;

import common.annotations.UserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ui.BaseUiTest;
import ui.pages.BankAlert;
import ui.pages.EditProfilePage;
import ui.pages.UserDashboard;

import static org.assertj.core.api.Assertions.assertThat;

public class UpdateUsernameTest extends BaseUiTest {
    @ParameterizedTest
    @ValueSource(strings = "Test Testov")
    @UserSession
    public void userCanUpdateNameToValidTest(String newName) {
        String initialName = SessionStorage.getSteps().getCustomerProfile().getName();

        new UserDashboard().open().usernameClick().getPage(EditProfilePage.class)
                .updateName(newName)
                .checkAlertAndAccept(BankAlert.NAME_UPDATED_SUCCESSFULLY.getMessage())
                .checkUpdatedName(newName);

        String updatedName = SessionStorage.getSteps().getCustomerProfile().getName();

        assertThat(updatedName).isEqualTo(newName);
        assertThat(updatedName).isNotEqualTo(initialName);
    }

    @ParameterizedTest
    @ValueSource(strings = "a")
    @UserSession
    public void userCannotUpdateNameToInvalidTest(String newName) {
        String initialName = SessionStorage.getSteps().getCustomerProfile().getName();

        new UserDashboard().open().usernameClick().getPage(EditProfilePage.class)
                .updateName(newName)
                // sometimes another error returned "❌ Please enter a valid name."
                .checkAlertAndAccept(BankAlert.ENTER_VALID_NAME.getMessage())
                .checkNotUpdatedName(newName);

        String actualName = SessionStorage.getSteps().getCustomerProfile().getName();

        assertThat(actualName).isEqualTo(initialName);
    }
}
