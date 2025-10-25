package api.iteration1;

import generators.RandomModelGenerator;
import models.CreateUserRequest;
import models.CreateUserResponse;
import models.comparison.ModelAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.skeleton.requesters.ValidatedCrudeRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.List;
import java.util.stream.Stream;

public class CreateUserTest extends BaseTest {

    @Test
    // TODO: add test about creating name with all valid characters (letters, digits, dashes, underscores, and dots)
    public void adminCanCreateUserWithCorrectDataTest() {
        CreateUserRequest createUserRequest = RandomModelGenerator.generate(CreateUserRequest.class);

        CreateUserResponse createUserResponse = new ValidatedCrudeRequester<CreateUserResponse>(
                Endpoint.ADMIN_USER,
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityIsCreated())
                .post(createUserRequest);

        // register user for further deletion
        registerCreatedUser(createUserResponse);

        ModelAssertions.assertThatModels(createUserRequest, createUserResponse).match();

    }

    public static Stream<Arguments> userInvalidData() {
        return Stream.of(
                // username field validation
                // blank username
                // it will return same errors texts, but in different orders. Hint: It's not a string but array of strings returned in the response
                Arguments.of(" ", "Password33%", "USER", "username",
                        List.of("Username cannot be blank", "Username must contain only letters, digits, dashes, underscores, and dots", "Username must be between 3 and 15 characters")), // Username must contain only letters, digits, dashes, underscores, and dots, Username must be between 3 and 15 characters, Username cannot be blank"
                // username consists of 2 characters
                Arguments.of("ab", "Password33%", "USER", "username",
                        List.of("Username must be between 3 and 15 characters")),
                // username consists of 16 characters
                Arguments.of("Abcdeftghjklthjg", "Password33%", "USER", "username",
                        List.of("Username must be between 3 and 15 characters")),
                // username contains not allowed symbol
                Arguments.of("Abc%", "Password33%", "USER", "username",
                        List.of("Username must contain only letters, digits, dashes, underscores, and dots")));
    }

    @ParameterizedTest
    @MethodSource("userInvalidData")
    public void adminCannotCreateUserWithInvalidDataTest(String username, String password, String role, String errorKey, List<String> errorValue) {
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(username)
                .password(password)
                .role(role)
                .build();

        new CrudRequester(
                Endpoint.ADMIN_USER,
                RequestSpecs.adminSpec(),
                ResponseSpecs.requestReturnsBadRequestJson(errorKey, errorValue))
                .post(createUserRequest);
    }

}
