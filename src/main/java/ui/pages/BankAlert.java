package ui.pages;

import lombok.Getter;

@Getter
public enum BankAlert {
    USER_CREATED_SUCCESSFULLY("✅ User created successfully!"),
    INVALID_CREDENTIALS("Invalid credentialsAxiosError: Request failed with status code 401"),
    USERNAME_MUST_BE_BETWEEN_3_AND_15_CHARACTERS("Username must be between 3 and 15 characters"),
    NEW_ACCOUNT_CREATED("✅ New Account Created! Account Number:"),
    MONEY_DEPOSITED("^✅ Successfully deposited \\$\\d+\\.\\d+ to account \\S+!$"),
    MONEY_NOT_DEPOSITED("❌ Please enter a valid amount."),
    MONEY_TRANSFERRED("✅ Successfully transferred \\$\\d+\\.\\d+ to account \\S+!"),
    NO_USER_FOUND_WITH_ACCOUNT_NUMBER("❌ No user found with this account number."),
    NAME_UPDATED_SUCCESSFULLY("✅ Name updated successfully!"),
    //ENTER_VALID_NAME("❌ Please enter a valid name."),
    ENTER_VALID_NAME("Name must contain two words with letters only");

    private final String message;

    BankAlert(String message) {
        this.message = message;
    }
}
