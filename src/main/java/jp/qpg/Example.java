package jp.qpg;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import jp.qpg.Person.Builder;
import jp.qpg.Person.Builder.F;
import jp.qpg.Person.Gender;

@SuppressWarnings("javadoc")
public class Example {
    public static void main(String[] args) {
        System.out.println("各フィールドを個別にセット");
        Person yamada = Person.of().set(F.name, "山田太郎").set(F.birthday, LocalDate.of(1987, 1, 2)).set(F.gender, Gender.MALE).get();
        System.out.println(yamada);

        System.out.println("他のインスタンスの値をコピーして変更");
        Person suzuki = Person.of(yamada).set(F.name, "鈴木真一").get();
        System.out.println(suzuki);
        System.out.println("当然コピー元は変更されない");
        System.out.println(yamada);

        System.out.println("任意のフィールドを一括設定");
        System.out.println(Person.of(F.name, "匿名希望", F.gender, Gender.FEMALE).get());

        System.out.println("Optionalのフィールドは設定しない場合自動でemptyを設定");
        System.out.println(Person.of(F.name, "名無し"));

        System.out.println("Optional.empty時の表示内容を変更");
        System.out.println(Person.of(F.name, "名無し").setup(b -> b.empty = "不明"));

        System.out.println("必須チェックのバリデータ設定");
        Builder builder = new Builder().setup(b -> b.validator = () -> Objects.requireNonNull(b.getValue(F.birthday), "誕生日は必須です。 - " + b));
        try {
            System.out.println(builder.set(F.name, "私").get());
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("値の変換設定");
        builder.reset().setup(b -> b.converter(F.birthday,
                value -> value instanceof CharSequence ? LocalDate.parse((CharSequence) value, DateTimeFormatter.ofPattern("yyyy/MM/dd")) : value));
        try {
            System.out.println(builder.set(F.birthday, "2017/01/11").get());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
