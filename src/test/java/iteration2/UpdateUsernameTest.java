package iteration2;

import generators.RandomData;
import iteration1.BaseTest;
import models.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.AdminCreateUserRequester;
import requests.GetCustomerProfileRequester;
import requests.UserUpdateNameRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;
import utils.Utilities;

import java.util.stream.Stream;

public class UpdateUsernameTest extends BaseTest {
    //Positive test: Authorised user can update their name to another valid name (2 words, only letters, devided by space)
    @Test
    public void userCanUpdateNameTest() {
        // prepare request for user creation
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getUserPassword())
                .role(UserRole.USER.toString())
                .build();

        // send user create request + extract name (=null)
        String initialName  = new AdminCreateUserRequester(RequestSpecs.adminSpec(), ResponseSpecs.entityIsCreated())
                .post(createUserRequest)
                .extract()
                .as(CreateUserResponse.class).getName();

        // prepare request for name update
        UserUpdateNameRequest userUpdateNameRequest = UserUpdateNameRequest.builder()
                .name(RandomData.getUsername())
                .build();

        // send name update request
        UserUpdateNameResponse userUpdateNameResponses = new UserUpdateNameRequester(
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .post(userUpdateNameRequest)
                .extract()
                .as(UserUpdateNameResponse.class);

        String actualName = userUpdateNameResponses.getCustomer().getName();
        String actualMessage = userUpdateNameResponses.getMessage();

        softly.assertThat(actualName).isEqualTo(userUpdateNameRequest.getName());
        softly.assertThat(actualMessage).isEqualTo(Utilities.PROFILE_UPDATED_MSG);

        // check that name has changed
        GetCustomerProfileResponse getCustomerProfileResponse = new GetCustomerProfileRequester(RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .post(null)
                .extract()
                .as(GetCustomerProfileResponse.class);

        String updatedName = getCustomerProfileResponse.getName();
        softly.assertThat(updatedName).isEqualTo(userUpdateNameRequest.getName());
        softly.assertThat(updatedName).isNotEqualTo(initialName);
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
        // prepare request for user creation
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getUserPassword())
                .role(UserRole.USER.toString())
                .build();

        // send user create request
        String initialName = new AdminCreateUserRequester(RequestSpecs.adminSpec(), ResponseSpecs.entityIsCreated())
                .post(createUserRequest)
                .extract()
                .as(CreateUserResponse.class).getName();

        // prepare request for name update
        UserUpdateNameRequest userUpdateNameRequest = UserUpdateNameRequest.builder()
                .name(updatedName)
                .build();

        // send name update request
        new UserUpdateNameRequester(
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsBadRequestText(message))
                .post(userUpdateNameRequest);

        // check that name has not changed
        String actualName = new GetCustomerProfileRequester(
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .post(null)
                .extract()
                .as(GetCustomerProfileResponse.class)
                .getName();

        softly.assertThat(actualName).isEqualTo(initialName);

    }
}
