package iteration1;

import generators.RandomData;
import models.CreateUserRequest;
import models.CreateUserResponse;
import models.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.AdminCreateUserRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.stream.Stream;

public class CreateUserTest extends BaseTest {

    @Test
    // TODO: add test about creating name with all valid characters (letters, digits, dashes, underscores, and dots)
    public void adminCanCreateUserWithCorrectDataTest() {
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getUserPassword())
                .role(UserRole.USER.toString())
                .build();

        CreateUserResponse createUserResponse = new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityIsCreated())
                .post(createUserRequest)
                .extract()
                .as(CreateUserResponse.class);

        softly.assertThat(createUserRequest.getUsername()).isEqualTo(createUserResponse.getUsername());
        softly.assertThat(createUserRequest.getPassword()).isNotEqualTo(createUserResponse.getPassword());
        softly.assertThat(createUserRequest.getRole()).isEqualTo(createUserResponse.getRole());

    }

    public static Stream<Arguments> userInvalidData() {
        return Stream.of(
                // username field validation
                // blank username
                // it will return same errors texts, but in different orders. Hint: It's not a string but array of strings returned in the response
                Arguments.of(" ", "Password33%", "USER", "username", "Username must contain only letters, digits, dashes, underscores, and dots"), // Username must contain only letters, digits, dashes, underscores, and dots, Username must be between 3 and 15 characters, Username cannot be blank"
                // username consists of 2 characters
                Arguments.of("ab", "Password33%", "USER", "username", "Username must be between 3 and 15 characters"),
                // username consists of 16 characters
                Arguments.of("Abcdeftghjklthjg", "Password33%", "USER", "username", "Username must be between 3 and 15 characters"),
                // username contains not allowed symbol
                Arguments.of("Abc%", "Password33%", "USER", "username", "Username must contain only letters, digits, dashes, underscores, and dots"));
    }

    @ParameterizedTest
    @MethodSource("userInvalidData")
    public void adminCannotCreateUserWithInvalidDataTest(String username, String password, String role, String errorKey, String errorValue) {

        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(username)
                .password(password)
                .role(role)
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.requestReturnsBadRequest(errorKey, errorValue))
                .post(createUserRequest);
        }

}
