package core.data.functions;

import core.data.TestDataFunction;

public class UpperCaseFunction implements TestDataFunction {

    @Override
    public String getName() {
        return "toUpperCase";
    }

    @Override
    public String execute(String[] args) {
        return args[0].toUpperCase();
    }
}