package core.data.functions;

import core.data.TestDataFunction;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ParseDateFunction implements TestDataFunction {

    private static final DateTimeFormatter TARGET_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public String getName() {
        return "parseDate";
    }

    @Override
    public String execute(String[] args) {
        if (args.length < 2) {
            throw new IllegalArgumentException(
                    "parseDate requires 2 arguments: date,sourceFormat. Example: ${parseDate:2026-05-28,yyyy-MM-dd}"
            );
        }

        DateTimeFormatter sourceFormatter =
                DateTimeFormatter.ofPattern(args[1].trim());

        LocalDate date = LocalDate.parse(args[0].trim(), sourceFormatter);

        return date.format(TARGET_FORMATTER);
    }
}