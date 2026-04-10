package ui.iteration1;

import api.models.UserGetAccountsResponse;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import common.utils.RetryUtils;
import org.junit.jupiter.api.Test;
import ui.BaseUiTest;
import ui.pages.BankAlert;
import ui.pages.UserDashboard;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateAccountTest extends BaseUiTest {
    @Test
    @UserSession
    public void userCanCreateAccountTest() {
        new UserDashboard().open().creteNewAccount();

        List<UserGetAccountsResponse> createdAccounts = RetryUtils.retry(
                () -> SessionStorage.getSteps().getAllAccounts(),
                accounts -> accounts.size() == 1,
                3,
                100);

        assertThat(createdAccounts).hasSize(1);

        new UserDashboard().checkAlertAndAccept(BankAlert.NEW_ACCOUNT_CREATED.getMessage() + createdAccounts.getFirst().getAccountNumber());

        assertThat(createdAccounts.getFirst().getBalance()).isZero();
    }
}
