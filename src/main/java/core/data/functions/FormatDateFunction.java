package core.data.functions;

import core.data.TestDataFunction;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class FormatDateFunction implements TestDataFunction {

    private static final DateTimeFormatter DEFAULT_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public String getName() {
        return "formatDate";
    }

    @Override
    public String execute(String[] args) {
        if (args.length < 2) {
            throw new IllegalArgumentException(
                    "formatDate requires 2 arguments: date,targetFormat. Example: ${formatDate:28/05/2026,yyyyMMdd}"
            );
        }

        LocalDate date = LocalDate.parse(args[0].trim(), DEFAULT_FORMATTER);
        DateTimeFormatter targetFormatter =
                DateTimeFormatter.ofPattern(args[1].trim());

        return date.format(targetFormatter);
    }
}