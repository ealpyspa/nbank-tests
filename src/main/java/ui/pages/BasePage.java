package ui.pages;

import api.models.CreateUserRequest;
import api.specs.RequestSpecs;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.Alert;
import ui.elements.BaseElement;

import java.util.List;
import java.util.function.Function;

import static com.codeborne.selenide.Selenide.*;
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

    public T checkAlertAndAcceptOneOf(String... expectedAlerts) {
        Alert alert = switchTo().alert();
        assertThat(alert.getText()).isIn((Object[]) expectedAlerts);
        alert.accept();
        return (T) this;
    }

    public T selectAccount(String accountNumber) {
        selectAccountDropdown.click();
        $(Selectors.byText(accountNumber)).click();
        return (T) this;
    }

    public static void authAsUser(String username, String password) {
        Selenide.open("/");
        String userAuthHeader = RequestSpecs.getUserAuthHeader(username, password);
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);
    }

    public static void authAsUser(CreateUserRequest createUserRequest) {
        authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword());
    }

    // ElementCollection -> List<BaseElement>
    protected <T extends BaseElement> List<T> generatePageElements(ElementsCollection collection, Function<SelenideElement, T> constructor) {
        return collection.stream().map(constructor).toList();
    }
}
