package core.data.functions;

import core.data.TestDataFunction;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LastDayOfPreviousMonthFunction implements TestDataFunction {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public String getName() {
        return "lastDayOfPreviousMonth";
    }

    @Override
    public String execute(String[] args) {
        LocalDate date = parseDate(args);

        return date
                .minusMonths(1)
                .withDayOfMonth(date.minusMonths(1).lengthOfMonth())
                .format(FORMATTER);
    }

    private LocalDate parseDate(String[] args) {
        if (args.length == 0 || args[0].isBlank()) {
            return LocalDate.now();
        }

        return LocalDate.parse(args[0].trim(), FORMATTER);
    }
}