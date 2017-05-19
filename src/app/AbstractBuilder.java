package app;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Builder base
 * 
 * @param <VALUE> Value type
 * @param <BUILDER> Builder type
 * @param <NAMES> Names type
 */
public class AbstractBuilder<VALUE, BUILDER extends AbstractBuilder<VALUE, BUILDER, NAMES>, NAMES extends Enum<?>> implements Supplier<VALUE> {

    /**
     * Text in case of "Optional.empty" in "toString"
     */
    public String empty = "(未設定)";
    /**
     * Key-value separator in "toString"
     */
    public String pairSeparator = ": ";
    /**
     * Entry separator in "toString"
     */
    public String entrySeparator = ", ";

    /**
     * Target class
     */
    final Class<VALUE> clazz;
    /**
     * Field names
     */
    final NAMES[] names;
    /**
     * Field refrections
     */
    final Field[] fields;
    /**
     * Field values
     */
    final Object[] values;

    /**
     * Cache
     */
    private static final Map<Class<?>, Cache> caches = new ConcurrentHashMap<>();

    /**
     * Cached items
     */
    private static class Cache {
        /**
         * Target class
         */
        private Class<?> clazz;
        /**
         * Field names
         */
        private Enum<?>[] names;
        /**
         * Field refrections
         */
        private Field[] fields;
    }

    /**
     * Constructor
     */
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

    /**
     * @param name Field name
     * @param value Field value
     * @return Self
     */
    @SuppressWarnings("unchecked")
    public BUILDER set(NAMES name, Object value) {
        int i = name.ordinal();
        values[i] = fields[i].getType() == Optional.class && !(value instanceof Optional) ? Optional.ofNullable(value) : value;
        return (BUILDER) this;
    }

    /**
     * @param source Source
     * @return Copied Builder
     */
    @SuppressWarnings("unchecked")
    public BUILDER set(VALUE source) {
        for (NAMES i : names) {
            try {
                set(i, fields[i.ordinal()].get(source));
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new InternalError(e);
            }
        }
        return (BUILDER) this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.function.Supplier#get()
     */
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

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @SuppressWarnings("unchecked")
    @Override
    public String toString() {
        return IntStream.range(0, values.length).mapToObj(i -> {
            Object v = values[i];
            return names[i] + pairSeparator + (v instanceof Optional ? ((Optional<Object>) v).orElse(empty) : v);
        }).collect(Collectors.joining(entrySeparator));
    }

    /**
     * @param setup Setup
     * @return Self
     */
    @SuppressWarnings("unchecked")
    public BUILDER setup(Consumer<BUILDER> setup) {
        setup.accept((BUILDER) this);
        return (BUILDER) this;
    }
}