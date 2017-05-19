package app;

import java.time.LocalDate;
import java.util.Optional;

@SuppressWarnings("javadoc")
public class Person {
    public final String name;
    public final LocalDate birthday;
    public final Optional<Gender> gender;

    public enum Gender {
        MALE,
        FEMALE
    };

    public Person(String name, LocalDate birthday, Optional<Gender> gender) {
        this.name = name;
        this.birthday = birthday;
        this.gender = gender;
    }

    public static class Builder extends AbstractBuilder<Person, Builder, Builder.F> {
        public static enum F {
            name,
            birthday,
            gender
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
