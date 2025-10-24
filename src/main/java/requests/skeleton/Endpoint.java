package requests.skeleton;

import lombok.AllArgsConstructor;
import lombok.Getter;
import models.*;

@AllArgsConstructor
@Getter
public enum Endpoint {
    ADMIN_USER(
            "/admin/users",
            CreateUserRequest.class,
            CreateUserResponse.class
    ),

    LOGIN (
            "/auth/login",
            LoginUserRequest.class,
            LoginUserResponse.class
    ),

    ACCOUNTS(
            "/accounts",
            BaseModel.class,
            CreateAccountResponse.class
    ),

    ACCOUNTS_DEPOSIT(
            "/accounts/deposit",
            DepositMoneyRequest.class,
            DepositMoneyResponse.class
    ),

    CUSTOMER_ACCOUNTS(
            "/customer/accounts",
            BaseModel.class,
            UserGetAccountsResponse.class
    ),

    ACCOUNTS_TRANSFER(
            "/accounts/transfer",
            TransferMoneyRequest.class,
            TransferMoneyResponse.class
    ),

    CUSTOMER_PROFILE_PUT(
            "/customer/profile",
            UserUpdateNameRequest.class,
            UserUpdateNameResponse.class
    ),

    CUSTOMER_PROFILE_GET(
            "/customer/profile",
            BaseModel.class,
            GetCustomerProfileResponse.class
    ),

    ADMIN_USER_DELETE(
            "/admin/users/{id}",
            BaseModel.class,
            BaseModel.class
    );

    private final String url;
    private final Class<? extends BaseModel> requestModel;
    private final Class<? extends BaseModel> responseModel;
}
