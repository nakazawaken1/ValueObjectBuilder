package jp.qpg;

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
     * Meta info
     */
    final Meta<VALUE, NAMES> meta;
    /**
     * Field values
     */
    final Object[] values;

    /**
     * Caches
     */
    private static final Map<Class<?>, Meta<?, Enum<?>>> caches = new ConcurrentHashMap<>();

    /**
     * Meta info
     * 
     * @param <T> Value type
     * @param <U> Names type
     */
    private static class Meta<T, U extends Enum<?>> {
        /**
         * Target class
         */
        private Class<T> clazz;
        /**
         * Field names
         */
        private U[] names;
        /**
         * Field reflections
         */
        private Field[] fields;
        /**
         * constructor
         */
        private Constructor<T> constructor;
    }

    /**
     * Constructor
     */
    @SuppressWarnings("unchecked")
    public AbstractBuilder() {
        meta = (Meta<VALUE, NAMES>) caches.computeIfAbsent(getClass(), key -> {
            Type[] types = ((ParameterizedType) key.getGenericSuperclass()).getActualTypeArguments();
            Meta<VALUE, NAMES> m = new Meta<>();
            m.clazz = (Class<VALUE>) types[0];
            m.names = ((Class<NAMES>) types[2]).getEnumConstants();
            m.fields = Stream.of(m.names).map(i -> {
                try {
                    return m.clazz.getField(i.name());
                } catch (NoSuchFieldException | SecurityException e) {
                    try {
                        Field field = m.clazz.getDeclaredField(i.name());
                        field.setAccessible(true);
                        return field;
                    } catch (NoSuchFieldException | SecurityException e2) {
                        throw new InternalError(e2);
                    }
                }
            }).toArray(Field[]::new);
            try {
                m.constructor = (Constructor<VALUE>) m.clazz.getDeclaredConstructor(Stream.of(m.fields).map(Field::getType).toArray(Class[]::new));
                m.constructor.setAccessible(true);
            } catch (NoSuchMethodException | SecurityException e) {
                throw new InternalError(e);
            }
            return (Meta<?, Enum<?>>) m;
        });
        values = Stream.of(meta.fields).map(field -> field.getType() == Optional.class ? Optional.empty() : null).toArray(Object[]::new);
    }

    /**
     * @param name Field name
     * @param value Field value
     * @return Self
     */
    @SuppressWarnings("unchecked")
    public BUILDER set(NAMES name, Object value) {
        int i = name.ordinal();
        values[i] = meta.fields[i].getType() == Optional.class && !(value instanceof Optional) ? Optional.ofNullable(value) : value;
        return (BUILDER) this;
    }

    /**
     * @param name Field name
     * @param value Field value
     * @param nameValues Name-value pairs
     * @return Self
     */
    @SuppressWarnings("unchecked")
    public BUILDER set(NAMES name, Object value, Object... nameValues) {
        set(name, value);
        for (int i = 0; i + 1 < nameValues.length; i += 2) {
            set((NAMES) nameValues[i], nameValues[i + 1]);
        }
        return (BUILDER) this;
    }

    /**
     * @param source Source
     * @return Copied Builder
     */
    @SuppressWarnings("unchecked")
    public BUILDER set(VALUE source) {
        for (NAMES i : meta.names) {
            try {
                set(i, meta.fields[i.ordinal()].get(source));
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
            return meta.constructor.newInstance(values);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
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
            return meta.names[i] + pairSeparator + (v instanceof Optional ? ((Optional<Object>) v).orElse(empty) : v);
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