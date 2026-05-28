package core.data.functions;

import core.data.TestDataFunction;

public class SysFunction implements TestDataFunction {

    @Override
    public String getName() {
        return "sys";
    }

    @Override
    public String execute(String[] args) {
        if (args.length < 1) {
            throw new IllegalArgumentException(
                    "sys requires at least 1 argument. Example: ${sys:user.dir}"
            );
        }

        String propertyName = args[0].trim();
        String defaultValue = args.length >= 2 ? args[1].trim() : "";

        return System.getProperty(propertyName, defaultValue);
    }
}