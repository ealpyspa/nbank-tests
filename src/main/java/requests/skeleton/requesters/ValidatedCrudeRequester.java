package requests.skeleton.requesters;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.BaseModel;
import requests.skeleton.Endpoint;
import requests.skeleton.HttpRequest;
import requests.skeleton.interfaces.CrudEndpointInterface;
import requests.skeleton.interfaces.GetAllEndpointInterface;

import java.io.IOException;
import java.util.List;

public class ValidatedCrudeRequester<T extends BaseModel> extends HttpRequest implements CrudEndpointInterface, GetAllEndpointInterface {
    public CrudRequester crudRequester;

    public ValidatedCrudeRequester(Endpoint endpoint, RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(endpoint, requestSpecification, responseSpecification);
        this.crudRequester = new CrudRequester(endpoint, requestSpecification, responseSpecification);
    }

    @Override
    public T post(BaseModel model) {
        return (T) crudRequester.post(model).extract().as(endpoint.getResponseModel());
    }

    @Override
    public Object get(long id) {
        return null;
    }

    @Override
    public Object update(BaseModel model) {
        Response response = crudRequester.update(model).extract().response();
        String body = response.asString();

        if (body.trim().startsWith("{") || body.trim().startsWith("[")) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                return mapper.readValue(body, endpoint.getResponseModel());
            } catch (IOException e) {
                throw new RuntimeException("Failed to parse JSON API response", e);
            }
        } else {
            return body;
        }
    }

    @Override
    public Object delete(long id) {
        var response = crudRequester.delete(id);
        String body = response.extract().asString();

        String trimmed = body.trim();
        if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
            return JsonPath.from(trimmed).getObject("", endpoint.getResponseModel());
        } else if (trimmed.isEmpty()) {
            return null;
        } else {
            return body; // plain text
        }
    }

    @Override
    public List<T> getAll() {
        Response response = crudRequester.getAll().extract().response();
        String json = response.asString();

        ObjectMapper mapper = new ObjectMapper();

        try {
            JsonNode rootNode = mapper.readTree(json);

            if (rootNode.isArray()) {
                JavaType listType = mapper.getTypeFactory()
                        .constructCollectionType(List.class, endpoint.getResponseModel());
                return mapper.readValue(json, listType);
            } else {
                // Wrap single object in a list for consistency
                T single = (T) mapper.readValue(json, endpoint.getResponseModel());
                return List.of(single);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to deserialize API response", e);
        }
    }
}
