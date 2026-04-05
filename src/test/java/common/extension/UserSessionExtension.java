package common.extension;

import api.iteration1.BaseTest;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.requests.steps.AdminSteps;
import api.requests.steps.RequestResponsePair;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import ui.pages.BasePage;

import java.util.LinkedList;
import java.util.List;

/**
 * The class is move to src/test/java from src/main/java in order to get access to BaseTest.registerCreatedUser() to delete created users
 */

public class UserSessionExtension implements BeforeEachCallback {
    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        UserSession annotation = extensionContext.getRequiredTestMethod().getAnnotation(UserSession.class);
        if (annotation == null) {
            return;
        }

        int userCount = annotation.value();
        int authAsUser = annotation.auth();

        SessionStorage.clear();
        List<CreateUserRequest> users = new LinkedList<>();

        for (int i = 0; i < userCount; i++) {
            RequestResponsePair<CreateUserRequest, CreateUserResponse> created = AdminSteps.createUser();
            users.add(created.getRequest());

            // Reuse API cleanup from BaseTest @AfterEach (cleanUpUsers)
            BaseTest.registerCreatedUser(created.getResponse());
        }

        SessionStorage.addUsers(users);
        BasePage.authAsUser(SessionStorage.getUser(authAsUser));
    }
}
