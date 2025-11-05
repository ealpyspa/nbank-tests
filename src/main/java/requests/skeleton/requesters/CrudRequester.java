package requests.skeleton.requesters;

import common.helper.StepLogger;
import configs.Config;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.BaseModel;
import requests.skeleton.Endpoint;
import requests.skeleton.HttpRequest;
import requests.skeleton.interfaces.CrudEndpointInterface;
import requests.skeleton.interfaces.GetAllEndpointInterface;

import static io.restassured.RestAssured.given;

public class CrudRequester extends HttpRequest implements CrudEndpointInterface, GetAllEndpointInterface {
    private final static String API_VERSION = Config.getProperty("apiVersion");
    public CrudRequester(Endpoint endpoint, RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(endpoint, requestSpecification, responseSpecification);
    }

    @Override
    public ValidatableResponse post(BaseModel model) {
        return StepLogger.log("POST request to " + endpoint.getUrl(), () -> {
        var body = model == null ? "" : model;
        return given()
                .spec(requestSpecification)
                .body(body)
                .post(API_VERSION + endpoint.getUrl())
                .then()
                .assertThat()
                .spec(responseSpecification);
        });
    }

    @Override
    public Object get(long id) {
        return null;
    }

    @Override
    public ValidatableResponse update(BaseModel model) {
        return StepLogger.log("PUT request to " + endpoint.getUrl(),
                () -> given()
                .spec(requestSpecification)
                .body(model)
                .put(API_VERSION + endpoint.getUrl())
                .then()
                .assertThat()
                .spec(responseSpecification)
        );
    }

    @Override
    public ValidatableResponse delete(long id) {
        return StepLogger.log("DELETE request to " + endpoint.getUrl(),
                () -> given()
                .spec(requestSpecification)
                .pathParam("id", id)
                .body("")
                .delete(API_VERSION + endpoint.getUrl())
                .then()
                .assertThat()
                .spec(responseSpecification)
        );
    }

    @Override
    public ValidatableResponse getAll() {
        return StepLogger.log("GET all request to " + endpoint.getUrl(),
                () -> given()
                .spec(requestSpecification)
                .body("")
                .get(API_VERSION + endpoint.getUrl())
                .then()
                .assertThat()
                .spec(responseSpecification)
        );
    }
}
