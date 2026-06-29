package core.data.functions;

import core.data.TestDataFunction;

public class ExtractAmountFunction implements TestDataFunction {

    @Override
    public String getName() {
        return "extractAmount";
    }

    @Override
    public String execute(String[] args) {
        if (args.length < 1) {
            throw new IllegalArgumentException(
                    "extractAmount requires 1 argument. Example: ${extractAmount:1 234.99 EUR}"
            );
        }

        String input = args[0];

        String amount = input
                .replaceAll("[^0-9,.-]", "")
                .replace(",", ".");

        if (amount.isBlank()) {
            throw new IllegalArgumentException(
                    "No amount found in input: " + input
            );
        }

        return amount;
    }
}