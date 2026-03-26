package ui.pages;

import com.codeborne.selenide.*;
import lombok.Getter;
import ui.elements.UserBadge;

import java.util.List;

import static com.codeborne.selenide.Selenide.$;

@Getter
public class AdminPanel extends BasePage<AdminPanel> {
    private SelenideElement adminPanelText = $(Selectors.byText("Admin Panel"));
    private SelenideElement addUserButton = $(Selectors.byText("Add User"));

    @Override
    public String url() {
        return "/admin";
    }

    public AdminPanel createUser(String username, String password) {
        usernameInput.sendKeys(username);
        passwordInput.sendKeys(password);
        addUserButton.click();
        return this;
    }

    public List<UserBadge> getAllUsers() {
        ElementsCollection elementsCollection = $(Selectors.byText("All Users")).parent().findAll("li");

        elementsCollection.shouldBe(CollectionCondition.sizeGreaterThan(0));

        return generatePageElements(elementsCollection, UserBadge::new);
    }
}
