package iteration1;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;

public class CreateUserTest {
    @BeforeAll
    public static void setupRestAssured() {
        RestAssured.filters(
                List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter()
                ));
    }

    @Test
    // TODO: add test about creating name with all valid characters (letters, digits, dashes, underscores, and dots)
    public void adminCanCreateUserWithCorrectDataTest() {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("""
                        {
                          "username": "kate2002",
                          "password":  "Kate2000!",
                          "role": "USER"
                        }
                        """)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .body("username", Matchers.equalTo("kate2002"))
                .body("password", Matchers.not(Matchers.equalTo("Kate2000!")))
                .body("role", Matchers.equalTo("USER"));
    }

    public static Stream<Arguments> userInvalidData() {
        return Stream.of(
                // username field validation
                // blank username
                // it will return same errors texts, but in different orders. Hint: It's not a string but array of strings returned in the response
                Arguments.of(" ", "Password33%", "USER", "username", "Username must contain only letters, digits, dashes, underscores, and dots, Username must be between 3 and 15 characters, Username cannot be blank"),
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
        String requestBody = String.format("""
                {
                          "username": "%s",
                          "password":  "%s",
                          "role": "%s"
                        }
                """, username, password, role);
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body(requestBody)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(errorKey,Matchers.hasItem(errorValue)); // changed equalsTo() to hasItem() as an array of error is returned, not one item
    }

}
