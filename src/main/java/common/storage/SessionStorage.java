package common.storage;

import api.models.CreateUserRequest;
import api.requests.steps.UserSteps;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class SessionStorage {
    /*
    ThreadLocal - way to make SessionStorage thread-save

    Each thread accesses the INSTANCE.get() and get its COPY

    Map<Thread, SessionStorage>

    Example:
    Test 1 creates users, add them to SessionStorage (its own COPY1), work with them
    Test 2 do the same -> creates users, add them to SessionStorage (its own COPY2), work with them
    Test 3 do the same -> creates users, add them to SessionStorage (its own COPY3), work with them
     */

    private static final ThreadLocal<SessionStorage> INSTANCE = ThreadLocal.withInitial(SessionStorage::new);

    private final LinkedHashMap<CreateUserRequest, UserSteps> userStepsMap = new LinkedHashMap<>();

    private SessionStorage() {
    }

    public static void addUsers(List<CreateUserRequest> users) {
        for (CreateUserRequest user : users) {
            INSTANCE.get().userStepsMap.put(user, new UserSteps(user.getUsername(), user.getPassword()));

        }
    }

    /**
     * Return object CreateUserRequest by its serial number in the list of created users.
     * @param number Serial number starting from 1, not 0
     * @return CreateUserRequest correlated to specified serial number
     */
    public static CreateUserRequest getUser(int number) {
        return new ArrayList<>(INSTANCE.get().userStepsMap.keySet()).get(number - 1);
    }

    public static CreateUserRequest getUser() {
        return getUser(1);
    }

    public static UserSteps getSteps(int index) {
        return new ArrayList<>(INSTANCE.get().userStepsMap.values()).get(index - 1);
    }

    public static UserSteps getSteps() {
        return getSteps(1);
    }

    public static void clear() {
        INSTANCE.get().userStepsMap.clear();
    }
}
