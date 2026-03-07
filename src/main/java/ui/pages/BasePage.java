package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.Alert;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.switchTo;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class BasePage<T extends BasePage> {
    protected SelenideElement usernameInput = $(Selectors.byAttribute("placeholder", "Username"));
    protected SelenideElement passwordInput = $(Selectors.byAttribute("placeholder", "Password"));
    protected SelenideElement selectAccountDropdown = $(Selectors.byText("-- Choose an account --"));
    protected SelenideElement amountInput = $(Selectors.byAttribute("placeholder", "Enter amount"));
    // think of Auth/Unauth pages (as only auth pages contains below element)
    protected SelenideElement username= $(".user-name");

    public abstract String url();

    public T open() {
        return Selenide.open(url(), (Class<T>) this.getClass());
    }

    public <T extends BasePage<?>> T getPage(Class<T> pageClass) {
        return Selenide.page(pageClass);
    }

    public T checkAlertAndAccept(String bankAlert) {
        Alert alert = switchTo().alert();
        assertThat(alert.getText()).contains(bankAlert);
        alert.accept();
        return (T) this;
    }

    public T checkAlertAndAcceptMatches(String bankAlert) {
        Alert alert = switchTo().alert();
        assertThat(alert.getText()).matches(bankAlert);
        alert.accept();
        return (T) this;
    }

    public T selectAccount(String accountNumber) {
        selectAccountDropdown.click();
        $(Selectors.byText(accountNumber)).click();
        return (T) this;
    }
}
