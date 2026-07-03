package core.data.functions;

import core.data.TestDataFunction;

import java.math.BigDecimal;

public class AddFunction implements TestDataFunction {

    @Override
    public String getName() {
        return "add";
    }

    @Override
    public String execute(String[] args) {
        BigDecimal first = new BigDecimal(args[0]);
        BigDecimal second = new BigDecimal(args[1]);

        return first.add(second).toString();
    }
}