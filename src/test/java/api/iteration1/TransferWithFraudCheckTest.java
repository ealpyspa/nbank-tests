package api.iteration1;

import api.fixtures.fraud.FraudCheckMock;
import api.models.CreateUserRequest;
import api.models.DepositMoneyRequest;
import api.models.TransferMoneyRequest;
import api.models.TransferMoneyResponse;
import api.models.comparison.ModelAssertions;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.ValidatedCrudeRequester;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class TransferWithFraudCheckTest extends BaseTest {

    @RegisterExtension
    FraudCheckMockExtension fraudExtension = new FraudCheckMockExtension();

    private FraudCheckMock fraudMock() {
        return fraudExtension.fraudCheck();
    }

    @Test
    public void testTransferWithFraudCheckApprovedTest() {

        fraudMock().approved();

        TransferScenario scenario = prepareTransfer();

        TransferMoneyResponse actual = executeTransfer(scenario);

        softly.assertThat(actual).isNotNull();

        TransferMoneyResponse expected = TransferMoneyResponse.builder()
                .status("APPROVED")
                .message("Transfer approved and processed immediately")
                .amount(scenario.transferAmount)
                .senderAccountId(scenario.senderAccountId)
                .receiverAccountId(scenario.receiverAccountId)
                .fraudRiskScore(0.2)
                .fraudReason("Low risk transaction")
                .requiresManualReview(false)
                .requiresVerification(false)
                .build();

        ModelAssertions.assertThatModels(expected, actual).match();
    }

    @Test
    public void testTransferWithFraudCheckBlockedTest() {

        fraudMock().blocked();

        TransferScenario scenario = prepareTransfer();

        TransferMoneyResponse actual = executeTransfer(scenario);

        softly.assertThat(actual.getStatus()).isEqualTo("BLOCKED");

        softly.assertThat(actual.isRequiresManualReview()).isFalse();
    }

    @Test
    public void testTransferWithFraudCheckReviewRequiredByDecisionTest() {

        fraudMock().manualReviewRequiredByDecision();

        TransferScenario scenario = prepareTransfer();

        TransferMoneyResponse actual = executeTransfer(scenario);

        softly.assertThat(actual.getStatus()).isEqualTo("MANUAL_REVIEW_REQUIRED");

        softly.assertThat(actual.isRequiresManualReview()).isFalse();
    }

    @Test
    public void testTransferWithFraudCheckReviewRequiredByFlagTest() {

        fraudMock().manualReviewRequiredByFlag();

        TransferScenario scenario = prepareTransfer();

        TransferMoneyResponse actual = executeTransfer(scenario);

        softly.assertThat(actual.getStatus()).isEqualTo("MANUAL_REVIEW_REQUIRED");

        softly.assertThat(actual.isRequiresManualReview()).isTrue();
    }

    @Test
    public void testTransferWithFraudCheckVerificationRequiredByDecision() {

        fraudMock().verificationRequiredByDecision();

        TransferScenario scenario = prepareTransfer();

        TransferMoneyResponse actual = executeTransfer(scenario);

        softly.assertThat(actual.getStatus()).isEqualTo("VERIFICATION_REQUIRED");

        softly.assertThat(actual.isRequiresVerification()).isFalse();
    }

    @Test
    public void testTransferWithFraudCheckVerificationRequiredByFlag() {

        fraudMock().verificationRequiredByFlag();

        TransferScenario scenario = prepareTransfer();

        TransferMoneyResponse actual = executeTransfer(scenario);

        softly.assertThat(actual.getStatus()).isEqualTo("APPROVED");

        softly.assertThat(actual.isRequiresVerification()).isTrue();
    }

    @Test
    public void testTransferWithFraudCheckTimoutTest() {

        fraudMock().timeout(5000);

        TransferScenario scenario = prepareTransfer();

        TransferMoneyResponse actual = executeTransfer(scenario);

        softly.assertThat(actual.getStatus()).isEqualTo("MANUAL_REVIEW_REQUIRED"); // not "REVIEW_REQUIRED" as "MANUAL_REVIEW_REQUIRED" is set by server

        softly.assertThat(actual.isRequiresManualReview()).isTrue();
    }

    @Test
    public void testTransferWithFraudCheckConnectionErrorTest() {

        fraudMock().connectionError();

        TransferScenario scenario = prepareTransfer();

        TransferMoneyResponse actual = executeTransfer(scenario);

        softly.assertThat(actual.getStatus()).isEqualTo("MANUAL_REVIEW_REQUIRED"); // not "REVIEW_REQUIRED" as "MANUAL_REVIEW_REQUIRED" is set by server

        softly.assertThat(actual.isRequiresManualReview()).isTrue();
    }

    @Test
    public void testTransferWithFraudCheckHttpErrorTest() {
        fraudMock().httpError(500);

        TransferScenario scenario = prepareTransfer();

        TransferMoneyResponse actual = executeTransfer(scenario);

        softly.assertThat(actual.getStatus()).isEqualTo("MANUAL_REVIEW_REQUIRED"); // not "REVIEW_REQUIRED" as "MANUAL_REVIEW_REQUIRED" is set by server

        softly.assertThat(actual.isRequiresManualReview()).isTrue();
    }

    private TransferScenario prepareTransfer() {
        var senderCreation = AdminSteps.createUser();
        CreateUserRequest sender = senderCreation.getRequest();

        registerCreatedUser(senderCreation.getResponse());

        long senderAccountId = UserSteps.userCreatesAccount(sender).getId();

        float depositAmount = (float) (Math.random() * 4999.9 + 0.1);

        DepositMoneyRequest depositMoneyRequest = DepositMoneyRequest.builder()
                .accountId(senderAccountId)
                .amount(depositAmount)
                .build();

        UserSteps.depositMoneyResponse(sender, depositMoneyRequest);

        var receiverCreation = AdminSteps.createUser();
        CreateUserRequest createUserRequest2 = receiverCreation.getRequest();

        registerCreatedUser(receiverCreation.getResponse());

        long receiverAccountId = UserSteps.userCreatesAccount(createUserRequest2).getId();

        float transferAmount = (float) (Math.random() * (depositAmount - 0.1) + 0.1);

        return new TransferScenario(
                sender,
                senderAccountId,
                receiverAccountId,
                transferAmount
        );
    }

    private TransferMoneyResponse executeTransfer(TransferScenario scenario) {
        return new ValidatedCrudeRequester<TransferMoneyResponse>(
                Endpoint.TRANSFER_WITH_FRAUD_CHECK,
                RequestSpecs.authAsUser(scenario.sender.getUsername(), scenario.sender.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .post(TransferMoneyRequest.builder()
                        .senderAccountId(scenario.senderAccountId)
                        .receiverAccountId(scenario.receiverAccountId)
                        .amount(scenario.transferAmount)
                        .build());
    }

    private record TransferScenario(
            CreateUserRequest sender,
            long senderAccountId,
            long receiverAccountId,
            float transferAmount
    ) {
    }
}
