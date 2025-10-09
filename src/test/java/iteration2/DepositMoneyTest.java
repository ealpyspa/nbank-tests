package iteration2;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
                Arguments.of("kate2077", "Kate2000!", "USER", Float.MIN_NORMAL),
                // maximum allowed amount of money
                Arguments.of("kate2078", "Kate2000!", "USER", 5000)
        );
    }

    // Positive test: Authorized user can top up their account with valid sum
    // Here is an issue: sometimes actual result is 1.1754944E-38 (when equalTo(balance) is used),
    // another time 1.1754944E-38F (when equalTo(1.1754944E-38) is hardcoded)
    // casting to double doesn't help
    // Question: can it be fixed in API, so it returns same result constantly?
    // Workaround solution: extract value and use assertEquals()
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
        float actualAmount = given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(request)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract().jsonPath().getFloat("transactions[0].amount");

        assertEquals(balance, actualAmount);

        // check that balance has changed
        float actualBalance = given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract().jsonPath().getFloat("[0].balance");

        assertEquals(balance, actualBalance);
    }
}
