package core.data.functions;

import core.data.TestDataFunction;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TodayIsoFunction implements TestDataFunction {

    @Override
    public String getName() {
        return "todayISO";
    }

    @Override
    public String execute(String[] args) {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }
}