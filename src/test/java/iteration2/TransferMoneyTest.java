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
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransferMoneyTest {
    @BeforeAll
    public static void setupRestAssured() {
        RestAssured.filters(
                List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter()
                ));
    }

    public static Stream<Arguments> validAmountOfMoney() {
        return Stream.of(
                // minimal sum of transfer
                Arguments.of("kate405", "Kate3001!", "USER", Float.MIN_NORMAL, "alice8", "Alice11!", "USER", Float.MIN_NORMAL),
                // maximum sum of transfer
                Arguments.of("kate406", "Kate3001!", "USER", 10000 + Float.MIN_NORMAL, "alice9", "Alice11!", "USER", 10000)
        );
    }

    @ParameterizedTest
    @MethodSource("validAmountOfMoney")
    // Positive test: User can transfer valid amount of money to another user's account
    public void useCanTransferMoneyToAnotherUserTest(String username, String password, String role, float balance, String username1, String password1, String role1, float amount) {
        // create user1 + extract auth token
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

        // user1 create their account
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

        // user1 top up their account with money (enough to make transfer)
        // as maximum sum transfer check -> do top up twice
        // 1st top up
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
                .statusCode(HttpStatus.SC_OK);
        // 2nd top up
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(request)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        // create user 2
        String requestBody1 = String.format("""
                {
                          "username": "%s",
                          "password":  "%s",
                          "role": "%s"
                        }
                """, username1, password1, role1);
        String userAuthHeader1 = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body(requestBody1)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .header("Authorization");

        // user2 create their account
        int userAccountId1 = given()
                .header("Authorization", userAuthHeader1)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .response().jsonPath().getInt("id");

        // user1 transfer valid amount of money to user2 account
        String transferRequestBody = "{\n" +
                "  \"senderAccountId\": " + userAccountId + " ,\n" +
                "  \"receiverAccountId\": " + userAccountId1 + ",\n" +
                "  \"amount\": " + amount + " \n" +
                "}";
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAuthHeader)
                .body(transferRequestBody)
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("amount", Matchers.equalTo(amount))
                .body("message",Matchers.equalTo("Transfer successful"));

        // check hat user1 account balance has changed
        float actualBalance = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAuthHeader)
                .when()
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract().jsonPath().getFloat("[0].balance");

        assertEquals(balance, actualBalance);
    }

}
