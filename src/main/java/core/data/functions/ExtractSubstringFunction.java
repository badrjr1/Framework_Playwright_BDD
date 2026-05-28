package core.data.functions;

import core.data.TestDataFunction;

public class ExtractSubstringFunction implements TestDataFunction {

    @Override
    public String getName() {
        return "extractSubstring";
    }

    @Override
    public String execute(String[] args) {
        if (args.length < 3) {
            throw new IllegalArgumentException(
                    "extractSubstring requires 3 arguments: input,start,length. Example: ${extractSubstring:Hello World,6,5}"
            );
        }

        String input = args[0];
        int start = Integer.parseInt(args[1].trim());
        int length = Integer.parseInt(args[2].trim());

        if (start < 0 || start >= input.length()) {
            throw new IllegalArgumentException("Invalid start index: " + start);
        }

        int end = Math.min(start + length, input.length());

        return input.substring(start, end);
    }
}