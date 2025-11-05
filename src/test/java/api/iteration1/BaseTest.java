package api.iteration1;

import models.CreateUserResponse;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import requests.steps.AdminSteps;

import java.util.ArrayList;
import java.util.List;

public class BaseTest {
    protected SoftAssertions softly;
    protected static final List<CreateUserResponse> createUserResponses = new ArrayList<>();

    @BeforeEach
    public void setupTest(){
        this.softly = new SoftAssertions();
    }

    @AfterEach
    public void afterTest() {
        softly.assertAll();
    }

    @AfterEach
    public void cleanUpUsers() {
        for (CreateUserResponse userResponse : createUserResponses) {
            AdminSteps.deleteUser(userResponse.getId());
        }
        createUserResponses.clear();
    }

    public static void registerCreatedUser(CreateUserResponse user) {
        createUserResponses.add(user);
    }

    // test build-and-push-tests.yml
    //
    //

}
