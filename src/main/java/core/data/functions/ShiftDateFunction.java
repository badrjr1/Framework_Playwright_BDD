package core.data.functions;

import core.data.TestDataFunction;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ShiftDateFunction implements TestDataFunction {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public String getName() {
        return "shiftDate";
    }

    @Override
    public String execute(String[] args) {
        if (args.length < 3) {
            throw new IllegalArgumentException(
                    "shiftDate requires 3 arguments: date,quantity,unit. Example: ${shiftDate:28/05/2026,10,days}"
            );
        }

        LocalDate date = LocalDate.parse(args[0].trim(), FORMATTER);
        int quantity = Integer.parseInt(args[1].trim());
        String unit = args[2].trim().toLowerCase();

        LocalDate result;

        switch (unit) {
            case "day":
            case "days":
                result = date.plusDays(quantity);
                break;

            case "week":
            case "weeks":
                result = date.plusWeeks(quantity);
                break;

            case "month":
            case "months":
                result = date.plusMonths(quantity);
                break;

            case "year":
            case "years":
                result = date.plusYears(quantity);
                break;

            default:
                throw new IllegalArgumentException(
                        "Unsupported shiftDate unit: " + unit
                );
        }

        return result.format(FORMATTER);
    }
}