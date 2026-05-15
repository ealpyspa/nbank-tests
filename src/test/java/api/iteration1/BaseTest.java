package api.iteration1;

import api.models.CreateUserResponse;
import common.extension.APIVersionExtension;
import common.extension.TimingExtension;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import api.requests.steps.AdminSteps;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(TimingExtension.class)
@ExtendWith(APIVersionExtension.class)
public class BaseTest {
    protected SoftAssertions softly;

    // Per-thread cleanup list to avoid cross-test interference in parallel mode
    protected static final ThreadLocal<List<CreateUserResponse>> createUserResponses = ThreadLocal.withInitial(ArrayList::new);

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
        for (CreateUserResponse userResponse : createUserResponses.get()) {
            try {
                AdminSteps.deleteUser(userResponse.getId());
            } catch (AssertionError e) {
                // If user is already deleted by backend/other flow, ignore 404 in teardown
                if (!e.getMessage().contains("Expected status code <200> but was <404>")) {
                    throw e;
                }
            }
        }
        createUserResponses.remove();
    }

    public static void registerCreatedUser(CreateUserResponse user) {
        createUserResponses.get().add(user);
    }
}
