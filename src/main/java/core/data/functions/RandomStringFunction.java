package core.data.functions;

import core.data.TestDataFunction;

import java.security.SecureRandom;

public class RandomStringFunction implements TestDataFunction {

    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public String getName() {
        return "string";
    }

    @Override
    public String execute(String[] args) {
        int size = args.length > 0 ? Integer.parseInt(args[0]) : 8;
        String type = args.length > 1 ? args[1].toLowerCase() : "alpha";

        String chars;

        switch (type) {
            case "numeric":
                chars = "0123456789";
                break;
            case "alnum":
                chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
                break;
            case "alpha":
            default:
                chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
                break;
        }

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < size; i++) {
            result.append(chars.charAt(RANDOM.nextInt(chars.length())));
        }

        return result.toString();
    }
}