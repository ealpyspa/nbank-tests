package api.fixtures.fraud;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.Fault;

import java.util.Locale;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class FraudCheckMock {

    private static final String FRAUD_ENDPOINT = "/fraud-check";

    private final WireMockServer server;

    public FraudCheckMock(WireMockServer server) {
        this.server = server;
    }

    private void stubSuccessfulResponse(
            String status,
            String decision,
            double riskScore,
            String reason,
            boolean requiresManualReview,
            boolean additionalVerificationRequired
    ) {

        String responseBody = String.format(
                Locale.US,
                """
                {
                  "status": "%s",
                  "decision": "%s",
                  "riskScore": %.1f,
                  "reason": "%s",
                  "requiresManualReview": %s,
                  "additionalVerificationRequired": %s
                }
                """,
                status,
                decision,
                riskScore,
                reason,
                requiresManualReview,
                additionalVerificationRequired
        );

        server.stubFor(
                post(urlPathEqualTo(FRAUD_ENDPOINT))
                        .willReturn(okJson(responseBody))
        );
    }

    public void approved() {
        stubSuccessfulResponse(
                "SUCCESS",
                "APPROVED",
                0.2,
                "Low risk transaction",
                false,
                false
        );
    }

    public void blocked() {
        stubSuccessfulResponse(
                "SUCCESS",
                "BLOCKED",
                0.9,
                "High risk transaction",
                false,
                false
        );
    }

    /**
     * decision = "MANUAL_REVIEW_REQUIRED" AND requiresManualReview != true
     */
    public void manualReviewRequiredByDecision() {
        stubSuccessfulResponse(
                "SUCCESS",
                "MANUAL_REVIEW_REQUIRED", // it is set by default by real service
                0.7,
                "Manual review required by decision",
                false,
                false
        );
    }

    /**
     * decision != "MANUAL_REVIEW_REQUIRED" AND requiresManualReview = true
     */
    public void manualReviewRequiredByFlag() {
        stubSuccessfulResponse(
                "SUCCESS",
                "MANUAL_REVIEW_REQUIRED", // it is set by default by real service
                0.7,
                "Manual review required because by requiresManualReview flag",
                true,
                false
                );
    }

    /**
     * decision = "VERIFICATION_REQUIRED" AND additionalVerificationRequired != true
     */
    public void verificationRequiredByDecision() {
        stubSuccessfulResponse(
                "SUCCESS",
                "VERIFICATION_REQUIRED",
                0.6,
                "Additional verification required",
                false,
                false
        );
    }

    /**
     * decision != "VERIFICATION_REQUIRED" AND additionalVerificationRequired = true
     */
    public void verificationRequiredByFlag() {
        stubSuccessfulResponse(
                "SUCCESS",
                "APPROVED",
                0.6,
                "Additional verification required",
                false,
                true
        );
    }

    public void httpError(int statusCode) {
        server.stubFor(
                post(urlPathEqualTo(FRAUD_ENDPOINT))
                        .willReturn(aResponse()
                                .withStatus(statusCode))
        );
    }

    public void timeout(int delayMilliseconds) {
        server.stubFor(
                post(urlPathEqualTo(FRAUD_ENDPOINT))
                        .willReturn(okJson("{}")
                                .withFixedDelay(delayMilliseconds))
        );
    }

    public void connectionError() {
        server.stubFor(
                post(urlPathEqualTo(FRAUD_ENDPOINT))
                        .willReturn(aResponse()
                                .withFault(
                                        Fault.CONNECTION_RESET_BY_PEER
                                )
                        )
        );
    }
}
