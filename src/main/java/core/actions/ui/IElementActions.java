package core.actions.ui;

public interface IElementActions {
    void login();
    void searchLocatorsInFileOfFolder(String fileName);
    void searchLocatorsInFileOfFolder(String fileName, String folderName);
    void click(String elementName);
    void write(String elementName, String value);
    void saveElementValue(String elementName, String variableName);
    void assertElementEquals(String elementName, String expectedValue);
    void assertElementValueEquals(String elementName, String expectedValue);
    void assertElementContains(String elementName, String expectedValue);
    void assertElementValueContains(String elementName, String expectedValue);
    void assertDropdownSelectedValue(String elementName, String expectedValue);
    void assertElementChecked(String elementName);
    void assertElementEnabled(String elementName);
    void assertElementDisabled(String elementName);
    void saveScreenshot(String screenshotName);
    void waitForElementVisible(String elementName);
    void switchToFrameByIndex(int index);
    void switchToParentFrame();
    void switchToFrameByName(String frameName);
    void switchToFrameById(String frameId);
    void clickSvgElement(String elementName);
    void selectIndexFromDropdown(String elementName, int index);
    void selectTextFromDropdown(String elementName, String text);
    void writeValueOfKey(String key, String elementName);
    void pressKeyOnElement(String key, String elementName);
    void scrollToTop();
    void scrollToBottom();
    void scrollToElement(String elementName);
    void waitSeconds(int seconds);
    void debug();

    void navigate(String baseUrl);
    String getText(String elementName);
    String getValue(String elementName);
    void assertElementVisible(String elementName);
    void assertUrlContains(String elementName);

}
