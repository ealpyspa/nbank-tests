package iteration2;

import generators.RandomModelGenerator;
import iteration1.BaseTest;
import models.*;
import models.comparison.ModelAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.ValidatedCrudeRequester;
import requests.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;
import utils.Utilities;

import java.util.stream.Stream;

public class UpdateUsernameTest extends BaseTest {
    //Positive test: Authorised user can update their name to another valid name (2 words, only letters, devided by space)
    @Test
    public void userCanUpdateNameTest() {
        // prepare request for user creation
        // CreateUserRequest createUserRequest = AdminSteps.createUser(); -> not applicable as need to extract "name"
        CreateUserRequest createUserRequest = RandomModelGenerator.generate(CreateUserRequest.class);

        // send user create request + extract name (expected: name=null) + for further extract userId for delete
        CreateUserResponse createUserResponse = new ValidatedCrudeRequester<CreateUserResponse>(
                Endpoint.ADMIN_USER,
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityIsCreated())
                .post(createUserRequest);

        String initialName = createUserResponse.getName();

        // prepare request for name update
        UserUpdateNameRequest userUpdateNameRequest = RandomModelGenerator.generate(UserUpdateNameRequest.class);

        // send name update request
        Object userUpdateNameResponse = new ValidatedCrudeRequester<UserUpdateNameResponse>(
                Endpoint.CUSTOMER_PROFILE_PUT,
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .update(userUpdateNameRequest);

        String actualName = null;
        String actualMessage = null;

        if (userUpdateNameResponse instanceof UserUpdateNameResponse response) {
            // API returns JSON
            actualName = response.getCustomer().getName();
            actualMessage = response.getMessage();
        } else if (userUpdateNameResponse instanceof String message) {
            // API returns plain text
            actualMessage = message;
        }

        ModelAssertions.assertThatModels(userUpdateNameRequest, userUpdateNameResponse); //name
        softly.assertThat(actualMessage).isEqualTo(Utilities.PROFILE_UPDATED_MSG);

        // check that name has changed
        String updatedName = new ValidatedCrudeRequester<GetCustomerProfileResponse>(
                Endpoint.CUSTOMER_PROFILE_GET,
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .getAll()
                .get(0).getName();

        softly.assertThat(updatedName).isEqualTo(userUpdateNameRequest.getName());
        softly.assertThat(updatedName).isNotEqualTo(initialName);

        // delete user by admin
        long userId = createUserResponse.getId();
        AdminSteps.deleteUser(userId);

    }

    public static Stream<Arguments> notValidName() {
        return Stream.of(
                // 3 spaces
                Arguments.of("   ", Utilities.PROFILE_UPDATED_ERROR_MSG),
                // 1 word
                Arguments.of("Zoe", Utilities.PROFILE_UPDATED_ERROR_MSG),
                // two words, but not allowed character
                Arguments.of("New Name!", Utilities.PROFILE_UPDATED_ERROR_MSG),
                //3 words
                Arguments.of("New Name Zoe", Utilities.PROFILE_UPDATED_ERROR_MSG),
                // 2 words but comma separated
                Arguments.of("New_Name", Utilities.PROFILE_UPDATED_ERROR_MSG),
                // leading space
                Arguments.of(" New Name", Utilities.PROFILE_UPDATED_ERROR_MSG),
                // trailing space
                Arguments.of("New Name ", Utilities.PROFILE_UPDATED_ERROR_MSG)
        );
    }

    //Negative test: Authorised user cannot update their name to not valid name
    @ParameterizedTest
    @MethodSource("notValidName")
    public void userCannotUpdateNameToNotValidTest(String updatedName, String message) {
        // run a new container to run the test
        // prepare request for user creation
        // CreateUserRequest createUserRequest = AdminSteps.createUser(); -> not applicable as need to extract "name"
        CreateUserRequest createUserRequest = RandomModelGenerator.generate(CreateUserRequest.class);

        // send user create request + extract name (expected: name=null) + for further extract userId for delete
        CreateUserResponse createUserResponse = new ValidatedCrudeRequester<CreateUserResponse>(
                Endpoint.ADMIN_USER,
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityIsCreated())
                .post(createUserRequest);

        String initialName = createUserResponse.getName();

        // prepare request for name update
        UserUpdateNameRequest userUpdateNameRequest = UserUpdateNameRequest.builder()
                .name(updatedName)
                .build();

        // send name update request
        new ValidatedCrudeRequester<UserUpdateNameResponse>(
                Endpoint.CUSTOMER_PROFILE_PUT,
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsBadRequestText(message))
                .update(userUpdateNameRequest);

        // check that name has not changed
        String actualName = new ValidatedCrudeRequester<GetCustomerProfileResponse>(
                Endpoint.CUSTOMER_PROFILE_GET,
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .getAll().get(0).getName();

        softly.assertThat(actualName).isEqualTo(initialName);

        // delete user by admin
        long userId = createUserResponse.getId();
        AdminSteps.deleteUser(userId);

    }
}
