package core.data.functions;

import core.data.TestDataFunction;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class FirstDayOfNextMonthFunction implements TestDataFunction {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public String getName() {
        return "firstDayOfNextMonth";
    }

    @Override
    public String execute(String[] args) {
        LocalDate date = parseDate(args);

        return date
                .plusMonths(1)
                .withDayOfMonth(1)
                .format(FORMATTER);
    }

    private LocalDate parseDate(String[] args) {
        if (args.length == 0 || args[0].isBlank()) {
            return LocalDate.now();
        }

        return LocalDate.parse(args[0].trim(), FORMATTER);
    }
}