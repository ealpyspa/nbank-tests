package api.specs;

import api.configs.Config;
import api.models.LoginUserRequest;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.CrudRequester;
import com.github.viclovsky.swagger.coverage.FileSystemOutputWriter;
import com.github.viclovsky.swagger.coverage.SwaggerCoverageRestAssured;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.viclovsky.swagger.coverage.SwaggerCoverageConstants.OUTPUT_DIRECTORY;

public class RequestSpecs {
    private static final ConcurrentHashMap<String, String> authHeaders = new ConcurrentHashMap<>(Map.of("admin", "Basic YWRtaW46YWRtaW4="));

    private RequestSpecs() {
    }

    private static RequestSpecBuilder defaultRequestBuilder() {
        return
                new RequestSpecBuilder()
                        .setContentType(ContentType.JSON)
                        .setAccept(ContentType.JSON)
                        .addFilters(List.of(
                                new RequestLoggingFilter(),
                                new ResponseLoggingFilter(),
                                new SwaggerCoverageRestAssured(new
                                        FileSystemOutputWriter(Paths.get("target/" + OUTPUT_DIRECTORY))),
                                new AllureRestAssured()
                        ))
                        .setBaseUri(Config.getProperty("apiBaseUrl"));
    }

    public static RequestSpecification unauthSpec() {
        return defaultRequestBuilder().build();
    }

    public static RequestSpecification adminSpec() {
        return defaultRequestBuilder().addHeader("Authorization", authHeaders.get("admin"))
                .build();
    }

    public static RequestSpecification authAsUser(String username, String password) {
        return defaultRequestBuilder()
                .addHeader("Authorization", getUserAuthHeader(username, password))
                .build();
    }

    public static String getUserAuthHeader(String username, String password) {
        return authHeaders.computeIfAbsent(username, key ->
                new CrudRequester(
                        Endpoint.LOGIN,
                        RequestSpecs.unauthSpec(),
                        ResponseSpecs.requestReturnsOk())
                        .post(LoginUserRequest.builder().username(username).password(password).build())
                        .extract()
                        .header("Authorization")
        );
    }
}
