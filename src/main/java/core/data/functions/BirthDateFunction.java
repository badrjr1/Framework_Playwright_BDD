package core.data.functions;

import core.data.TestDataFunction;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class BirthDateFunction implements TestDataFunction {

    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public String getName() {
        return "birthDate";
    }

    @Override
    public String execute(String[] args) {
        int minAge = Integer.parseInt(args[0]);
        int maxAge = Integer.parseInt(args[1]);

        int age = minAge + RANDOM.nextInt(maxAge - minAge + 1);

        LocalDate birthDate = LocalDate.now()
                .minusYears(age)
                .minusDays(RANDOM.nextInt(365));

        return birthDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}