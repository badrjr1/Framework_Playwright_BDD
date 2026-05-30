package core.heal.locator;

public class LocatorDefinition {

    private final String type;
    private final String value;
    private final String option;

    public LocatorDefinition(String type, String value, String option) {
        this.type = type;
        this.value = value;
        this.option = option;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public String getOption() {
        return option;
    }

    public String toStorageFormat() {
        if (option == null || option.isEmpty()) {
            return type + "|" + value;
        }

        return type + "|" + value + "|" + option;
    }
}
