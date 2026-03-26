package ui.iteration2;

import api.generators.RandomModelGenerator;
import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import api.models.DepositMoneyRequest;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import ui.BaseUiTest;
import ui.pages.BankAlert;
import ui.pages.MakeTransferPage;
import ui.pages.UserDashboard;

import static org.assertj.core.api.Assertions.assertThat;

public class TransferMoneyTest extends BaseUiTest {
    @ParameterizedTest
    @CsvSource("1, 1")
    public void userCanMakeTransferToAccountTest(float balance, float amount) {
        CreateUserRequest user1 = AdminSteps.createUser().getRequest();

        CreateAccountResponse createAccountResponse1 = UserSteps.userCreatesAccount(user1);

        long createdAccountId = createAccountResponse1.getId();

        String createdAccountNumber = createAccountResponse1.getAccountNumber();

        DepositMoneyRequest depositMoneyRequest = DepositMoneyRequest.builder()
                .id(createdAccountId)
                .balance(balance)
                .build();

        UserSteps.depositMoneyResponse(user1, depositMoneyRequest);

        float afterDepositBalance = new UserSteps(user1.getUsername(), user1.getPassword()).getAllAccounts().getFirst().getBalance();

        CreateUserRequest user2 = AdminSteps.createUser().getRequest();

        CreateAccountResponse createAccountResponse2 = UserSteps.userCreatesAccount(user2);

        String createdAccount2Number = createAccountResponse2.getAccountNumber();

        float beforeTransferBalance = createAccountResponse2.getBalance();

        //authAsUser(user1);

        // Alert checks by pattern (without amount and account2Number checks)
        new UserDashboard().open().makeTransferClick().getPage(MakeTransferPage.class)
                .makeTransfer(createdAccountNumber, user2.getUsername(), createdAccount2Number, String.valueOf(amount))
                .checkAlertAndAcceptMatches(BankAlert.MONEY_TRANSFERRED.getMessage());

        float afterTransferBalance1 = new UserSteps(user1.getUsername(), user1.getPassword()).getAllAccounts().getFirst().getBalance();

        assertThat(afterTransferBalance1).isEqualTo(afterDepositBalance - amount);

        float afterTransferBalance2 = new UserSteps(user2.getUsername(), user2.getPassword()).getAllAccounts().getFirst().getBalance();

        assertThat(afterTransferBalance2).isEqualTo(beforeTransferBalance + amount);
    }

    @ParameterizedTest
    @CsvSource("1, 1")
    public void userCannotMakeTransferToNotExistingAccount(float amount, float balance) {
        CreateUserRequest user1 = AdminSteps.createUser().getRequest();

        CreateAccountResponse createAccountResponse1 = UserSteps.userCreatesAccount(user1);

        long createdAccountId = createAccountResponse1.getId();

        String createdAccountNumber = createAccountResponse1.getAccountNumber();

        DepositMoneyRequest depositMoneyRequest = DepositMoneyRequest.builder()
                .id(createdAccountId)
                .balance(balance)
                .build();

        UserSteps.depositMoneyResponse(user1, depositMoneyRequest);

        float afterDepositBalance = new UserSteps(user1.getUsername(), user1.getPassword()).getAllAccounts().getFirst().getBalance();

        String randomUsernameAndAccountNumber = RandomModelGenerator.generate(CreateUserRequest.class).getUsername(); // same value for randomUserAccount

        //authAsUser(user1);

        new UserDashboard().open().makeTransferClick().getPage(MakeTransferPage.class)
                .makeTransfer(createdAccountNumber, randomUsernameAndAccountNumber, randomUsernameAndAccountNumber, String.valueOf(amount))
                .checkAlertAndAccept(BankAlert.NO_USER_FOUND_WITH_ACCOUNT_NUMBER.getMessage());

        float afterTransferBalance1 = new UserSteps(user1.getUsername(), user1.getPassword()).getAllAccounts().getFirst().getBalance();

        assertThat(afterTransferBalance1).isEqualTo(afterDepositBalance);
    }
}
