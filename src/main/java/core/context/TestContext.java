package core.context;

import java.util.HashMap;
import java.util.Map;

public class TestContext {

    private static final ThreadLocal<Map<String, Object>> context =
            ThreadLocal.withInitial(HashMap::new);

    public static void put(String key, Object value) {
        context.get().put(key, value);
    }

    public static Object get(String key) {
        return context.get().get(key);
    }

    public static String getString(String key) {
        Object value = context.get().get(key);
        return value == null ? null : value.toString();
    }

    public static boolean contains(String key) {
        return context.get().containsKey(key);
    }

    public static void clear() {
        context.get().clear();
    }
}