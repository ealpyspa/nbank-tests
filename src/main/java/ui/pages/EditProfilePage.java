package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.refresh;
import static org.assertj.core.api.Assertions.assertThat;

public class EditProfilePage extends BasePage<EditProfilePage> {
    private SelenideElement newNameInput = $(Selectors.byAttribute("placeholder", "Enter new name"));
    private SelenideElement saveChangesButton = $(Selectors.byText("\uD83D\uDCBE Save Changes"));

    @Override
    public String url() {
        return "/edit-profile";
    }

    public EditProfilePage updateName(String newName) {
        newNameInput.shouldBe(Condition.editable).click();

        newNameInput.shouldBe(Condition.focused);

        newNameInput.clear();
        newNameInput.shouldHave(Condition.exactValue(""));

        newNameInput.sendKeys(newName);
        newNameInput.shouldHave(Condition.exactValue(newName));

        saveChangesButton.click();
        return this;
    }

    public EditProfilePage checkUpdatedName(String newName) {
        refresh();
        assertThat(username.getText()).isEqualTo(newName);
        return this;
    }

    public EditProfilePage checkNotUpdatedName(String newName) {
        refresh();
        assertThat(username.getText()).isNotEqualTo(newName);
        return this;
    }
}
