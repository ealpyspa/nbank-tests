package iteration2;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;

public class DepositMoneyTest {
    @BeforeAll
    public static void setupRestAssured() {
        RestAssured.filters(
                List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter()
                ));
    }

    public static Stream<Arguments> validAmountOfMoney() {
        return Stream.of(
                // minimal allowed amount of money
                Arguments.of("kate207", "Kate2000!", "USER", Float.MIN_NORMAL),
                // maximum allowed amount of money
                Arguments.of("kate208", "Kate2000!", "USER", 5000)
        );
    }

    // Positive test: Authorized user can top up their account with valid sum
    @ParameterizedTest
    @MethodSource("validAmountOfMoney")
    public void userCanTopUpAccount(String username, String password, String role, float balance) {
        // create user + extract auth token
        String requestBody = String.format("""
                {
                          "username": "%s",
                          "password":  "%s",
                          "role": "%s"
                        }
                """, username, password, role);
        String userAuthHeader = given()
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

        // create bank account and get account id
        int userAccountId = given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .response().jsonPath().getInt("id");

        // user top up account with money
        String request = "{\n" +
                "  \"id\": " + userAccountId + ",\n" +
                "  \"balance\": " + balance + "\n" +
                "}";
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(request)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("transactions[0].amount",Matchers.equalTo(balance));
    }
}
