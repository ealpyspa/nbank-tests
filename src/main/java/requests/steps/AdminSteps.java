package requests.steps;

import common.helper.StepLogger;
import generators.RandomModelGenerator;
import models.BaseModel;
import models.CreateUserRequest;
import models.CreateUserResponse;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.ValidatedCrudeRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class AdminSteps {
    public static RequestResponsePair<CreateUserRequest, CreateUserResponse> createUser() {
        CreateUserRequest userRequest = RandomModelGenerator.generate(CreateUserRequest.class);

        return StepLogger.log("Admin creates user: " + userRequest.getUsername(), () -> {

        CreateUserResponse userResponse = new ValidatedCrudeRequester<CreateUserResponse>(
                Endpoint.ADMIN_USER,
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityIsCreated())
                .post(userRequest);

        return new RequestResponsePair<>(userRequest, userResponse);
        });
    }

    public static void deleteUser(long id) {
        StepLogger.log("Admin deletes user with id: " + id , () -> {

        new ValidatedCrudeRequester<BaseModel>(
                Endpoint.ADMIN_USER_DELETE,
                RequestSpecs.adminSpec(),
                ResponseSpecs.requestReturnsOk())
                .delete(id);
        });
    }
}
