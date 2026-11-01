package tagalog;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Map<String, Object> values = new HashMap<>();
    private final Environment enclosing;

    public Environment() {
        this.enclosing = null;
    }

    public Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    public void define(String name, Object value) {
        values.put(name, value);
    }

    public boolean has(String name) {
        if (values.containsKey(name)) return true;
        return enclosing != null && enclosing.has(name);
    }

    public Object get(String name) {
        if (values.containsKey(name)) return values.get(name);
        if (enclosing != null) return enclosing.get(name);
        throw new IllegalStateException("Walang variable na '" + name + "'");
    }
}
