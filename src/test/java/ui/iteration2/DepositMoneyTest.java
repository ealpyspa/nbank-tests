package ui.iteration2;

import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import ui.BaseUiTest;
import ui.pages.BankAlert;
import ui.pages.DepositMoneyPage;
import ui.pages.UserDashboard;

import static org.assertj.core.api.Assertions.assertThat;

public class DepositMoneyTest extends BaseUiTest {
    @ParameterizedTest
    @CsvSource("1")
    public void userCanDepositMoneyWithValidSumTest(float amount) {
        CreateUserRequest user = AdminSteps.createUser().getRequest();

        CreateAccountResponse createAccountResponse = UserSteps.userCreatesAccount(user);

        float initialBalance = createAccountResponse.getBalance();

        String createdAccountNumber = createAccountResponse.getAccountNumber();

        authAsUser(user);

        // Alert checks by pattern (without amount and accountNumber checks)
        new UserDashboard().open().depositMoneyClick().getPage(DepositMoneyPage.class)
                .depositMoney(createdAccountNumber, amount)
                .checkAlertAndAcceptMatches(BankAlert.MONEY_DEPOSITED.getMessage());

        float actualBalance = new UserSteps(user.getUsername(),user.getPassword())
                .getAllAccounts().getFirst().getBalance();

        assertThat(actualBalance).isEqualTo(initialBalance + amount);
    }

    @ParameterizedTest
    @CsvSource("0")
    public void userCannotMakeDepositWithInvalidSumTest(float amount) {
        CreateUserRequest user = AdminSteps.createUser().getRequest();

        CreateAccountResponse createAccountResponse = UserSteps.userCreatesAccount(user);

        float initialBalance = createAccountResponse.getBalance();

        String createdAccountNumber = createAccountResponse.getAccountNumber();

        authAsUser(user);

        new UserDashboard().open().depositMoneyClick().getPage(DepositMoneyPage.class)
                .depositMoney(createdAccountNumber,amount)
                .checkAlertAndAccept(BankAlert.MONEY_NOT_DEPOSITED.getMessage());

        float actualBalance = new UserSteps(user.getUsername(),user.getPassword())
                .getAllAccounts().getFirst().getBalance();

        assertThat(actualBalance).isEqualTo(initialBalance);
    }
}
