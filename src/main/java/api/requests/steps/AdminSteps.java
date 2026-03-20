package api.requests.steps;

import common.helper.StepLogger;
import api.generators.RandomModelGenerator;
import api.models.BaseModel;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.ValidatedCrudeRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import java.util.List;

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

    public static List<CreateUserResponse> getAllUsers() {
        return new ValidatedCrudeRequester<CreateUserResponse>(
                Endpoint.ADMIN_USER,
                RequestSpecs.adminSpec(),
                ResponseSpecs.requestReturnsOk()).getAll(CreateUserResponse[].class);
    }
}
