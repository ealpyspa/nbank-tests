package api.iteration1;

import api.dao.AccountDao;
import api.dao.comparison.DaoModelAssertions;
import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import api.requests.steps.DataBaseSteps;
import api.requests.steps.UserSteps;
import common.annotations.APIVersion;
import org.junit.jupiter.api.Test;
import api.requests.steps.AdminSteps;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateAccountTest extends BaseTest {

    @Test
    @APIVersion("with_database")
    public void userCanCreateAccountTest() {
        // create user
        var createUser = AdminSteps.createUser();
        CreateUserRequest userRequest = createUser.getRequest();

        // register user for further deletion
        registerCreatedUser(createUser.getResponse());

        // send create user request + retrieve created account number
        CreateAccountResponse createdAccount = UserSteps.userCreatesAccount(userRequest);
        String createdAccountNumber1 = createdAccount.getAccountNumber();

        // GET request + assert that created account via POST is equal to account via GET
        String createdAccountNumber2 = new UserSteps(userRequest.getUsername(), userRequest.getPassword()).getAllAccounts().getFirst().getAccountNumber();
        assertThat(createdAccountNumber2).isEqualTo(createdAccountNumber1);

        // Database check that account is created: select * from accounts where account number = createdAccountNumber1
        // + assert that createdAccount (DTO/api) is equal created accountDAO
        AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(createdAccountNumber1);
        DaoModelAssertions.assertThat(createdAccount, accountDao).match();
    }
}
