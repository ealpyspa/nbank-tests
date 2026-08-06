package api.iteration1;

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
import common.annotations.FraudCheckMock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(FraudCheckMockExtension.class)
public class TransferWithFraudCheckTest extends BaseTest{

    @Test
    @FraudCheckMock(
            status = "SUCCESS",
            decision = "APPROVED",
            riskScore = 0.2,
            reason = "Low risk transaction",
            requiresManualReview = false,
            additionalVerificationRequired = false
    )
    public void testTransferWithFraudCheckTest() {
        //preconditions
        var createUser1 = AdminSteps.createUser();
        CreateUserRequest createUserRequest1 = createUser1.getRequest();

        registerCreatedUser(createUser1.getResponse());

        long createdAccountId1 = UserSteps.userCreatesAccount(createUserRequest1).getId();

        float depositAmount =(float) (Math.random() * 4999.9 + 0.1);

        DepositMoneyRequest depositMoneyRequest = DepositMoneyRequest.builder()
                .accountId(createdAccountId1)
                .amount(depositAmount)
                .build();

        UserSteps.depositMoneyResponse(createUserRequest1, depositMoneyRequest);

        var createUser2 = AdminSteps.createUser();
        CreateUserRequest createUserRequest2 = createUser2.getRequest();

        registerCreatedUser(createUser2.getResponse());

        long createdAccountId2 = UserSteps.userCreatesAccount(createUserRequest2).getId();

        float transferAmount = (float) (Math.random() * (depositAmount - 0.1) + 0.1);

        // test steps
        TransferMoneyResponse transferMoneyResponse = new ValidatedCrudeRequester<TransferMoneyResponse>(
                Endpoint.TRANSFER_WITH_FRAUD_CHECK,
                RequestSpecs.authAsUser(createUserRequest1.getUsername(), createUserRequest1.getPassword()),
                ResponseSpecs.requestReturnsOk())
                .post(TransferMoneyRequest.builder()
                        .senderAccountId(createdAccountId1)
                        .receiverAccountId(createdAccountId2)
                        .amount(transferAmount)
                        .build());

        softly.assertThat(transferMoneyResponse).isNotNull();

        TransferMoneyResponse expectedTransferMoneyResponse = TransferMoneyResponse.builder()
                .status("APPROVED")
                .message("Transfer approved and processed immediately")
                .amount(transferAmount)
                .senderAccountId(createdAccountId1)
                .receiverAccountId(createdAccountId2)
                .fraudRiskScore(0.2)
                .fraudReason("Low risk transaction")
                .requiresManualReview(false)
                .requiresVerification(false)
                .build();

        ModelAssertions.assertThatModels(expectedTransferMoneyResponse, transferMoneyResponse).match();
    }
}
