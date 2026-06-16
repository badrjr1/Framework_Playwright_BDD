package core.data.functions;

import core.data.TestDataFunction;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class TodayFunction implements TestDataFunction {

    @Override
    public String getName() {
        return "today";
    }

    @Override
    public String execute(String[] args) {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}