package api.iteration1;

import api.fixtures.fraud.FraudCheckMock;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class FraudCheckMockExtension implements BeforeEachCallback, AfterEachCallback {

    private static final int PORT = 8080;

    private WireMockServer wireMockServer;
    private FraudCheckMock fraudCheckMock;

    @Override
    public void beforeEach(ExtensionContext extensionContext) {

        wireMockServer = new WireMockServer(
                WireMockConfiguration
                        .wireMockConfig()
                        .port(PORT)
        );

        wireMockServer.start();

        fraudCheckMock = new FraudCheckMock(wireMockServer);
    }

    public FraudCheckMock fraudCheck() {
        return fraudCheckMock;
    }

    public String getBaseUrl() {
        return "http://host.docker.internal:" + wireMockServer.port();
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) {

        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }
}
