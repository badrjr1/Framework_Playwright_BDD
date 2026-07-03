package core.data.functions;

import core.data.TestDataFunction;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class NirppFunction implements TestDataFunction {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final DateTimeFormatter INPUT_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public String getName() {
        return "nirpp";
    }

    @Override
    public String execute(String[] args) {
        if (args.length < 2) {
            throw new IllegalArgumentException(
                    "nirpp requires 2 arguments: gender,birthDate. Example: ${nirpp:1,12/03/1997}"
            );
        }

        String gender = args[0].trim();

        if (!gender.equals("1") && !gender.equals("2")) {
            throw new IllegalArgumentException("nirpp gender must be 1 or 2");
        }

        LocalDate birthDate = LocalDate.parse(args[1].trim(), INPUT_FORMATTER);

        String year = String.format("%02d", birthDate.getYear() % 100);
        String month = String.format("%02d", birthDate.getMonthValue());

        String department = String.format("%02d", 1 + RANDOM.nextInt(95));
        String commune = String.format("%03d", 1 + RANDOM.nextInt(999));
        String order = String.format("%03d", 1 + RANDOM.nextInt(999));

        String base = gender + year + month + department + commune + order;

        int key = 97 - (int) (Long.parseLong(base) % 97);

        return base + String.format("%02d", key);
    }
}