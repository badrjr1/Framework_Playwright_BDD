package core.data.functions;

import core.data.TestDataFunction;

import java.security.SecureRandom;

public class SiretFunction implements TestDataFunction {

    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public String getName() {
        return "siret";
    }

    @Override
    public String execute(String[] args) {
        String siren = new SirenFunction().execute(new String[0]);
        String nic = generateNumericString(4);
        String base = siren + nic;
        int checkDigit = calculateLuhnCheckDigit(base);

        return base + checkDigit;
    }

    private String generateNumericString(int length) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < length; i++) {
            result.append(RANDOM.nextInt(10));
        }

        return result.toString();
    }

    private int calculateLuhnCheckDigit(String number) {
        int sum = 0;
        boolean doubleDigit = true;

        for (int i = number.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(number.charAt(i));

            if (doubleDigit) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }

            sum += digit;
            doubleDigit = !doubleDigit;
        }

        return (10 - (sum % 10)) % 10;
    }
}