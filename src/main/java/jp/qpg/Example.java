package jp.qpg;

import java.time.LocalDate;

import jp.qpg.Person.Builder.F;
import jp.qpg.Person.Gender;

@SuppressWarnings("javadoc")
public class Example {
    public static void main(String[] args) {
        Person yamada = Person.of().set(F.name, "山田太郎").set(F.birthday, LocalDate.of(1987, 1, 2)).set(F.gender, Gender.MALE).get();
        System.out.println(yamada);
        Person tanaka = Person.of(yamada).set(F.name, "田中陽子").set(F.gender, Gender.FEMALE).get();
        System.out.println(tanaka);
        System.out.println(yamada);
        System.out.println(Person.of(F.name, "匿名希望").get());
    }
}
