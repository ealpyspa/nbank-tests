package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import static com.codeborne.selenide.Selenide.$;

@Getter
public class UserDashboard extends BasePage<UserDashboard>{
    private SelenideElement welcomeText = $(Selectors.byClassName("welcome-text"));
    private SelenideElement createNewAccount = $(Selectors.byText("➕ Create New Account"));
    private SelenideElement depositMoneyButton = $(Selectors.byText("\uD83D\uDCB0 Deposit Money"));
    private SelenideElement makeTransferButton = $(Selectors.byText("\uD83D\uDD04 Make a Transfer"));

    @Override
    public String url() {
        return "/dashboard";
    }

    public UserDashboard creteNewAccount() {
        createNewAccount.click();
        return this;
    }

    public UserDashboard depositMoneyClick() {
        depositMoneyButton.click();
        return this;
    }

    public UserDashboard makeTransferClick() {
        makeTransferButton.click();
        return this;
    }

    public UserDashboard usernameClick() {
        username.click();
        return this;
    }
}
