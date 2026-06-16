package core.data;

import core.data.functions.*;

import java.util.HashMap;
import java.util.Map;

public class TestDataFunctionRegistry {

    private static final Map<String, TestDataFunction> FUNCTIONS = new HashMap<>();

    static {
        register(new TodayFunction());
        register(new TodayIsoFunction());
        register(new RandomStringFunction());
        register(new BirthDateFunction());

        register(new SirenFunction());
        register(new SiretFunction());
        register(new NirppFunction());

        register(new FirstDayOfYearFunction());
        register(new LastDayOfYearFunction());
        register(new FirstDayOfMonthFunction());
        register(new LastDayOfMonthFunction());

        register(new ShiftDateFunction());
        register(new FormatDateFunction());
        register(new ParseDateFunction());
        register(new LastDayOfPreviousMonthFunction());
        register(new FirstDayOfNextMonthFunction());

        register(new UpperCaseFunction());
        register(new LowerCaseFunction());
        register(new ExtractSubstringFunction());

        register(new SysFunction());

        register(new AddFunction());
        register(new ExtractAmountFunction());
    }

    private static void register(TestDataFunction function) {
        FUNCTIONS.put(function.getName(), function);
    }

    public static TestDataFunction get(String name) {
        TestDataFunction function = FUNCTIONS.get(name);

        if (function == null) {
            throw new RuntimeException("Fonction de données inconnue : " + name);
        }

        return function;
    }
}