package core.data.functions;

import core.data.TestDataFunction;

public class LowerCaseFunction implements TestDataFunction {

    @Override
    public String getName() {
        return "toLowerCase";
    }

    @Override
    public String execute(String[] args) {
        return args[0].toLowerCase();
    }
}