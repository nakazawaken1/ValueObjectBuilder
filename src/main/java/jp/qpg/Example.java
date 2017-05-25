package jp.qpg;

import java.time.LocalDate;

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
    }
}
