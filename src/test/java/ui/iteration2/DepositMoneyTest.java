package ui.iteration2;

import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import api.requests.steps.UserSteps;
import common.annotations.APIVersion;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.parallel.ResourceAccessMode;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import ui.BaseUiTest;
import ui.pages.BankAlert;
import ui.pages.DepositMoneyPage;
import ui.pages.UserDashboard;

import static org.assertj.core.api.Assertions.assertThat;

@ResourceLock(value = "accounts-write", mode = ResourceAccessMode.READ_WRITE)
public class DepositMoneyTest extends BaseUiTest {
    @ParameterizedTest
    @APIVersion("with_validation_fix")
    @CsvSource("1")
    @UserSession
    public void userCanDepositMoneyWithValidSumTest(float amount) {
        CreateUserRequest user = SessionStorage.getUser();

        CreateAccountResponse createAccountResponse = UserSteps.userCreatesAccount(user);
        float initialBalance = createAccountResponse.getBalance();
        String createdAccountNumber = createAccountResponse.getAccountNumber();

        // Alert checks by pattern (without amount and accountNumber checks)
        new UserDashboard().open().depositMoneyClick().getPage(DepositMoneyPage.class)
                .depositMoney(createdAccountNumber, amount)
                .checkAlertAndAcceptMatches(BankAlert.MONEY_DEPOSITED.getMessage());

        float actualBalance = SessionStorage.getSteps().getAllAccounts().getFirst().getBalance();
        assertThat(actualBalance).isEqualTo(initialBalance + amount);
    }

    @ParameterizedTest
    @CsvSource("0")
    @UserSession
    public void userCannotMakeDepositWithInvalidSumTest(float amount) {
        CreateUserRequest user = SessionStorage.getUser();

        CreateAccountResponse createAccountResponse = UserSteps.userCreatesAccount(user);
        float initialBalance = createAccountResponse.getBalance();
        String createdAccountNumber = createAccountResponse.getAccountNumber();

        new UserDashboard().open().depositMoneyClick().getPage(DepositMoneyPage.class)
                .depositMoney(createdAccountNumber,amount)
                .checkAlertAndAccept(BankAlert.MONEY_NOT_DEPOSITED.getMessage());

        float actualBalance = SessionStorage.getSteps().getAllAccounts().getFirst().getBalance();
        assertThat(actualBalance).isEqualTo(initialBalance);
    }
}
