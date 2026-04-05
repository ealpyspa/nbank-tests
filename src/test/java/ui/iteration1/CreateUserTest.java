package ui.iteration1;

import api.generators.RandomModelGenerator;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.models.comparison.ModelAssertions;
import api.requests.steps.AdminSteps;
import common.annotations.AdminSession;
import org.junit.jupiter.api.Test;
import ui.BaseUiTest;
import ui.elements.UserBadge;
import ui.pages.AdminPanel;
import ui.pages.BankAlert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CreateUserTest extends BaseUiTest {
    @Test
    @AdminSession
    public void adminCanCreateUserTest() {
        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);

        UserBadge newUSerBadge = new AdminPanel().open().createUser(newUser.getUsername(), newUser.getPassword())
                .checkAlertAndAccept(BankAlert.USER_CREATED_SUCCESSFULLY.getMessage())
                .findUserByUsername(newUser.getUsername());

        assertThat(newUSerBadge)
                .as("UserBadge should exist on Dashboard after user creation.").isNotNull();

        CreateUserResponse createdUser = AdminSteps.getAllUsers().stream()
                .filter(user -> user.getUsername().equals(newUser.getUsername()))
                .findFirst().get();

        registerCreatedUser(createdUser);

        ModelAssertions.assertThatModels(newUser, createdUser).match();
    }

    @Test
    @AdminSession
    public void adminCannotCreateUserWithInvalidDataTest() {
        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);
        newUser.setUsername("a");

        assertTrue(new AdminPanel().open().createUser(newUser.getUsername(), newUser.getPassword())
                .checkAlertAndAccept(BankAlert.USERNAME_MUST_BE_BETWEEN_3_AND_15_CHARACTERS.getMessage())
                .getAllUsers().stream().noneMatch(userBadge -> userBadge.getUsername().equals(newUser.getUsername())));

        long usersWithSameUserNameAsNewUser = AdminSteps.getAllUsers().stream()
                .filter(user -> user.getUsername().equals(newUser.getUsername()))
                .count();

        assertThat(usersWithSameUserNameAsNewUser).isZero();
    }
}
