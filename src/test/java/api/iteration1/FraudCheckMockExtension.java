package api.iteration1;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import common.annotations.FraudCheckMock;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Locale;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class FraudCheckMockExtension implements BeforeEachCallback, AfterEachCallback {

    private WireMockServer wireMockServer;

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        FraudCheckMock mockConfig = extensionContext.getTestMethod()
                .map(method -> method.getAnnotation(FraudCheckMock.class))
                .orElseGet(() -> extensionContext.getTestClass()
                        .map(clazz -> clazz.getAnnotation(FraudCheckMock.class))
                        .orElse(null));

        if (mockConfig != null) {
            setupWireMock(mockConfig);
        }
    }

    private void setupWireMock(FraudCheckMock config) {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(config.port()));
        wireMockServer.start();
        WireMock.configureFor("0.0.0.0", config.port());

        // Create the response body based on annotation parameters
        String responseBody = String.format(Locale.US, "{\n" +
                        "  \"status\": \"%s\",\n" +
                        "  \"decision\": \"%s\",\n" +
                        "  \"riskScore\": %.1f,\n" +
                        "  \"reason\": \"%s\",\n" +
                        "  \"requiresManualReview\": %s,\n" +
                        "  \"additionalVerificationRequired\": %s\n" +
                        "}",
                config.status(),
                config.decision(),
                config.riskScore(),
                config.reason(),
                config.requiresManualReview(),
                config.additionalVerificationRequired());

        // Mock the fraud detection service endpoint
        stubFor(post(urlPathMatching(config.endpoint()))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) throws Exception {

        if(wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    public String getBaseUrl() {
        if (wireMockServer != null) {
            return "http://host.docker.internal:" + wireMockServer.port();
        }
        return null;
    }
}
