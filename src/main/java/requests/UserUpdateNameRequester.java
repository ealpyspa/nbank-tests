package requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.UserUpdateNameRequest;

import static io.restassured.RestAssured.given;

public class UserUpdateNameRequester extends Request<UserUpdateNameRequest> {
    public UserUpdateNameRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    @Override
    public ValidatableResponse post(UserUpdateNameRequest model) {
        return given()
                .spec(requestSpecification)
                .body(model)
                .put("/api/v1/customer/profile")
                .then()
                .spec(responseSpecification);
    }
}
