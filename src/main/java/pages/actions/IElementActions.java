package pages.actions;

public interface IElementActions {
    void click(String elementName);
    void write(String elementName, String value);
    String getText(String elementName);
    String getValue(String elementName);
    void assertUrlContains(String elementName, boolean expected);
    void assertElementChecked(String elementName);
    void assertElementEnabled(String elementName);
    void assertElementVisible(String elementName);
    void assertElementEquals(String elementName, String expectedValue, boolean expectedStatus);
    void assertElementValueContains(String elementName, String expectedValue, boolean expectedStatus);
    void assertElementContains(String elementName, String expectedValue, boolean expectedStatus);

}
