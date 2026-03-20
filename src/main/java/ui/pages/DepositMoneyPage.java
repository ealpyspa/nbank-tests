package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;

public class DepositMoneyPage extends BasePage<DepositMoneyPage> {
    private SelenideElement depositButton = $(Selectors.byText("\uD83D\uDCB5 Deposit"));

    @Override
    public String url() {
        return "/deposit";
    }

    public DepositMoneyPage depositMoney(String accountNumber, float amount) {
        selectAccount(accountNumber);
        amountInput.sendKeys(String.valueOf(amount));
        depositButton.click();
        return this;
    }
}
