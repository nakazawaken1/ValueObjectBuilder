package app;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("javadoc")
public class AbstractBuilder<VALUE, BUILDER extends AbstractBuilder<VALUE, BUILDER, NAMES>, NAMES extends Enum<?>> implements Supplier<VALUE> {

    public String empty = "(未設定)";
    public String pairSeparator = ": ";
    public String entrySeparator = ", ";

    final Class<VALUE> clazz;
    final NAMES[] names;
    final Field[] fields;
    final Object[] values;

    private static final Map<Class<?>, Cache> caches = new ConcurrentHashMap<>();

    private static class Cache {
        private Class<?> clazz;
        private Enum<?>[] names;
        private Field[] fields;
    }

    @SuppressWarnings("unchecked")
    public AbstractBuilder() {
        Cache cache = caches.computeIfAbsent(getClass(), key -> {
            Type[] types = ((ParameterizedType) key.getGenericSuperclass()).getActualTypeArguments();
            Cache c = new Cache();
            c.clazz = (Class<?>) types[0];
            c.names = ((Class<Enum<?>>) types[2]).getEnumConstants();
            c.fields = Stream.of(c.names).map(i -> {
                try {
                    return c.clazz.getField(i.name());
                } catch (NoSuchFieldException | SecurityException e) {
                    throw new InternalError(e);
                }
            }).toArray(Field[]::new);
            return c;
        });
        clazz = (Class<VALUE>) cache.clazz;
        names = (NAMES[]) cache.names;
        fields = cache.fields;
        values = Stream.of(fields).map(field -> field.getType() == Optional.class ? Optional.empty() : null).toArray(Object[]::new);
    }

    @SuppressWarnings("unchecked")
    public BUILDER set(NAMES name, Object value) {
        int i = name.ordinal();
        values[i] = fields[i].getType() == Optional.class ? Optional.ofNullable(value) : value;
        return (BUILDER) this;
    }

    @SuppressWarnings("unchecked")
    public BUILDER copy(VALUE value) {
        for (NAMES i : names) {
            try {
                set(i, fields[i.ordinal()].get(value));
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new InternalError(e);
            }
        }
        return (BUILDER) this;
    }

    @Override
    public VALUE get() {
        try {
            Constructor<VALUE> constructor = clazz.getConstructor(Stream.of(fields).map(Field::getType).toArray(Class[]::new));
            return constructor.newInstance(values);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
                | SecurityException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public String toString(VALUE value) {
        return Stream.of(fields).map(field -> {
            try {
                Object v = field.get(value);
                return field.getName() + pairSeparator + (v instanceof Optional ? ((Optional<Object>) v).orElse(empty) : v);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.joining(entrySeparator));
    }
}