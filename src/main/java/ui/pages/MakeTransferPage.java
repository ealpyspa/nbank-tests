package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;

public class MakeTransferPage extends BasePage<MakeTransferPage>{
    private SelenideElement recipientNameInput = $(Selectors.byAttribute("placeholder","Enter recipient name"));
    private SelenideElement recipientAccountNumberInput = $(Selectors.byAttribute("placeholder","Enter recipient account number"));
    private SelenideElement confirmCheckbox = $(Selectors.byId("confirmCheck"));
    private SelenideElement sendTransferButton = $(Selectors.byText("\uD83D\uDE80 Send Transfer"));

    @Override
    public String url() {
        return "/transfer";
    }

   public MakeTransferPage makeTransfer(String senderAccountNumber, String name, String recipientAccountNumber, String amount) {
        selectAccount(senderAccountNumber);
        recipientNameInput.sendKeys(name);
        recipientAccountNumberInput.sendKeys(recipientAccountNumber);
        amountInput.sendKeys(amount);
        confirmCheckbox.click();
        sendTransferButton.click();
        return this;
    }
}
