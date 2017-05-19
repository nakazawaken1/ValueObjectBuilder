package app;

import java.time.LocalDate;
import java.util.Optional;

@SuppressWarnings("javadoc")
public class Person {
    public final String name;
    public final LocalDate birthday;
    public final Optional<Jender> jender;

    public enum Jender {
        MALE,
        FEMALE
    };

    public Person(String name, LocalDate birthday, Optional<Jender> jender) {
        this.name = name;
        this.birthday = birthday;
        this.jender = jender;
    }

    public static class Builder extends AbstractBuilder<Person, Builder, Builder.F> {
        public static enum F {
            name,
            birthday,
            jender
        }
    }

    public static Builder of() {
        return new Builder();
    }

    public static Builder of(Person source) {
        return new Builder().copy(source);
    }

    @Override
    public String toString() {
        return new Builder().toString(this);
    }
}
