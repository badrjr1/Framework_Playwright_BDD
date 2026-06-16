package core.data;

import core.context.TestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DynamicValueResolver {

    private static final Logger logger = LoggerFactory.getLogger(DynamicValueResolver.class);

    private static final Pattern FUNCTION_PATTERN =
            Pattern.compile("\\$\\{([a-zA-Z0-9_]+):(.*?)\\}");

    private static final Pattern CONTEXT_PATTERN =
            Pattern.compile("\\$\\{([a-zA-Z0-9_]+)\\}");

    public static String resolve(String value) {
        if (value == null) {
            return null;
        }

        String result = resolveContextVariables(value);
        result = resolveFunctions(result);

        return result;
    }

    private static String resolveContextVariables(String value) {
        Matcher matcher = CONTEXT_PATTERN.matcher(value);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String variableName = matcher.group(1);
            String variableValue = TestContext.getString(variableName);

            if (variableValue == null) {
                continue;
            }

            logger.debug("[DATA] Context variable resolved | {}={}", variableName, variableValue);

            matcher.appendReplacement(buffer, Matcher.quoteReplacement(variableValue));
        }

        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private static String resolveFunctions(String value) {
        Matcher matcher = FUNCTION_PATTERN.matcher(value);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String functionName = matcher.group(1);
            String rawArgs = matcher.group(2);

            String[] args = rawArgs == null || rawArgs.isBlank()
                    ? new String[0]
                    : rawArgs.split(",");

            for (int i = 0; i < args.length; i++) {
                args[i] = args[i].trim();
            }

            TestDataFunction function = TestDataFunctionRegistry.get(functionName);
            String functionResult = function.execute(args);

            TestContext.put(functionName + "?", functionResult);

            logger.info(
                    "[DATA] Dynamic function executed | function: {} | result: {}",
                    functionName,
                    functionResult
            );

            matcher.appendReplacement(buffer, Matcher.quoteReplacement(functionResult));
        }

        matcher.appendTail(buffer);
        return buffer.toString();
    }
}