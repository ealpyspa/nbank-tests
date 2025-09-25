package iteration2;

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

public class UpdateUsernameTest {
    @BeforeAll
    public static void setupRestAssured() {
        RestAssured.filters(
                List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter()
                ));
    }

    //Positive test: Authorised user can update their name to another valid name (2 words, only letters, devided by space)
    @Test
    public void userCanUpdateNameTest() {
        // create user + extract auth token
        String userAuthHeader = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("""
                        {
                          "username": "eva2007",
                          "password":  "Eva2000!",
                          "role": "USER"
                        }
                        """)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .header("Authorization");

        // update name
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAuthHeader)
                .body("""
                        {
                          "name": "N m"
                        }
                        """)
                .put("http://localhost:4111/api/v1/customer/profile")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("message", Matchers.equalTo("Profile updated successfully"))
                .body("customer.name", Matchers.equalTo("N m"));
    }

    public static Stream<Arguments> notValidName() {
        return Stream.of(
                // 3 spaces
                Arguments.of("zoe11", "Zoe1234!", "USER", "   "),
                // 1 word
                Arguments.of("zoe12", "Zoe1234!", "USER", "Zoe"),
                // two words, but not allowed character
                Arguments.of("zoe13", "Zoe1234!", "USER", "New Name!"),
                //3 words
                Arguments.of("zoe14", "Zoe1234!", "USER", "New Name Zoe"),
                // 2 words but comma separated
                Arguments.of("zoe15", "Zoe1234!", "USER", "New_Name"),
                // leading space
                Arguments.of("zoe16", "Zoe1234!", "USER", " New Name"),
                // trailing space
                Arguments.of("zoe17", "Zoe1234!", "USER", "New Name ")
        );
    }

    //Negative test: Authorised user cannot update their name to not valid name
    @ParameterizedTest
    @MethodSource("notValidName")
    public void userCannotUpdateNameToNotValidTest(String username, String password, String role, String updatedName) {
        // create user + extract auth token
        String requestBody = String.format("""
                {
                          "username": "%s",
                          "password":  "%s",
                          "role": "%s"
                        }
                """, username, password, role);
        String userAuthToken = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body(requestBody)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .header("Authorization");

        //update name
        String updateRequest = "{\n" +
                "\"name\": " + updatedName + "\n" +
                "}";
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAuthToken)
                .body(updateRequest)
                .put("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("status", Matchers.equalTo(400))
                .body("error", Matchers.equalTo("Bad Request"));
    }

}
