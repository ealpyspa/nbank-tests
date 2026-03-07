package ui.iteration2;

import api.generators.RandomModelGenerator;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.ValidatedCrudeRequester;
import api.requests.steps.UserSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
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

        authAsUser(user);

        new UserDashboard().open().usernameClick().getPage(EditProfilePage.class)
                .updateName(newName)
                .checkAlertAndAccept(BankAlert.NAME_UPDATED_SUCCESSFULLY.getMessage())
                .checkUpdatedName(newName);

        String updatedName = new UserSteps(user.getUsername(), user.getPassword()).getCustomerProfile().getName();

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

        authAsUser(user);

        new UserDashboard().open().usernameClick().getPage(EditProfilePage.class)
                .updateName(newName)
                // sometimes another error returned "❌ Please enter a valid name."
                .checkAlertAndAccept(BankAlert.ENTER_VALID_NAME.getMessage())
                .checkNotUpdatedName(newName);

        String actualName = new UserSteps(user.getUsername(), user.getPassword()).getCustomerProfile().getName();

        assertThat(actualName).isEqualTo(initialName);
    }
}
