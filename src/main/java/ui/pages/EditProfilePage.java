package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import common.utils.RetryUtils;
import org.openqa.selenium.Alert;
import org.openqa.selenium.Keys;

import java.util.Arrays;

import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.Assertions.assertThat;

public class EditProfilePage extends BasePage<EditProfilePage> {
    private SelenideElement newNameInput = $(Selectors.byAttribute("placeholder", "Enter new name"));
    private SelenideElement saveChangesButton = $(Selectors.byText("\uD83D\uDCBE Save Changes"));

    @Override
    public String url() {
        return "/edit-profile";
    }

    public EditProfilePage updateNameExpectSuccess(String newName) {
        return updateNameExpectingAlert(newName, BankAlert.NAME_UPDATED_SUCCESSFULLY.getMessage());
    }

    public EditProfilePage updateNameExpectInvalid(String newName, String... expectedInvalidAlerts) {
        return updateNameExpectingAlert(newName, expectedInvalidAlerts);
    }

    private EditProfilePage updateNameExpectingAlert(String newName, String... expectedAlerts) {
        final String[] lastInputValue = {null};
        final String[] lastAlertText = {null};

        try {
            RetryUtils.retry(
                    () -> {
                        newNameInput.shouldBe(Condition.visible, Condition.editable).click();
                        newNameInput.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
                        newNameInput.sendKeys(newName);
                        newNameInput.pressTab();

                        lastInputValue[0] = newNameInput.getValue();

                        saveChangesButton.shouldBe(Condition.visible, Condition.enabled).click();

                        Alert alert = switchTo().alert();
                        String text = alert.getText();
                        lastAlertText[0] = text;

                        boolean isExpected = Arrays.stream(expectedAlerts).anyMatch(text::contains);
                        if (!isExpected) {
                            alert.accept();
                        }
                        return text;
                    },
                    text -> text != null && Arrays.stream(expectedAlerts).anyMatch(text::contains),
                    3,
                    300
            );
        } catch (Exception e) {
            throw new AssertionError(
                    "Failed to get expected alert after retries. expectedAlerts=" + Arrays.toString(expectedAlerts) +
                            ", expectedName='" + newName + "', actualInput='" + lastInputValue[0] +
                            "', lastAlert='" + lastAlertText[0] + "'",
                    e
            );
        }

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
