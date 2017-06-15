# ValueObjectBuilder
Value object builder for java8

[Maven usage]

	<project>
	  ...
	  <repositories>
	    <repository>
	      <id>qpg.jp</id>
	      <name>qpg.jp repository</name>
	      <url>http://qpg.jp/maven</url>
	    </repository>
	  </repositories>
	  <dependencies>
	    <dependency>
	      <groupId>jp.qpg</groupId>
	      <artifactId>ValueObjectBuilder</artifactId>
	      <version>1.0.0</version>
	    </dependency>
	  </dependencies>
	</project>

[Example]

(Example.java)

	import java.time.LocalDate;
	import java.time.format.DateTimeFormatter;
	import java.util.Objects;
	
	import jp.qpg.Person;
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

(Run Example)

	各フィールドを個別にセット
	name: 山田太郎, birthday: 1987-01-02, gender: MALE
	他のインスタンスの値をコピーして変更
	name: 鈴木真一, birthday: 1987-01-02, gender: MALE
	当然コピー元は変更されない
	name: 山田太郎, birthday: 1987-01-02, gender: MALE
	任意のフィールドを一括設定
	name: 匿名希望, birthday: null, gender: FEMALE
	Optionalのフィールドは設定しない場合自動でemptyを設定
	name: 名無し, birthday: null, gender: (未設定)
	Optional.empty時の表示内容を変更
	name: 名無し, birthday: null, gender: 不明
	必須チェックのバリデータ設定
	値の変換設定
	java.lang.NullPointerException: 誕生日は必須です。 - name: 私, birthday: null, gender: (未設定)
		at java.util.Objects.requireNonNull(Objects.java:228)
		at Example.lambda$3(Example.java:34)
		at jp.qpg.AbstractBuilder.get(AbstractBuilder.java:225)
		at Example.main(Example.java:36)
	name: null, birthday: 2017-01-11, gender: (未設定)
