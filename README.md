**Лаборатори №8 — Mock объектууд: Mockito-гоор хамаарлыг орлуулж тестлэх**

- Оюутны нэр: Бат-Эрдэнэ Дөлгөөн
- Оюутны код: B242270136

Хичээл: F.CSA313 — Программ хангамжийн чанарын баталгаа ба тест, 2026 он.

Энэ ажилд `CourseRegistrationService.register()`-ийн логикийг JUnit 5 болон Mockito ашиглан нэгжийн түвшинд шалгасан. `StudentRepository`, `EmailSender` интерфейсүүдийг mock болгож, бодит өгөгдлийн сан болон SMTP сервергүйгээр тестүүдийг ажиллуулсан. `Student` өгөгдлийн объектыг бодитоор үүсгэсэн.

**Ашигласан орчин.** OpenJDK 25.0.5, Apache Maven 3.10.0, JUnit Jupiter 5.10.2, Mockito 5.11.0 ашигласан. `pom.xml` дотор Java-ийн хөрвүүлэлтийн түвшин `maven.compiler.release=17` гэж тохируулагдсан.

`java -version` командын бодит гаралт:

```text
openjdk version "25.0.5" 2026-10-20
OpenJDK Runtime Environment (build 25.0.5)
OpenJDK 64-Bit Server VM (build 25.0.5, mixed mode, sharing)
```

`mvn -version` командын бодит гаралт:

```text
Apache Maven 3.10.0 (c43a36b8d67be7e0805a411bc0898af1a51f5472)
Maven home: /usr/share/java/maven
Java version: 25.0.5, vendor: Arch Linux, runtime: /usr/lib/jvm/java-25-openjdk
Default locale: en, platform encoding: UTF-8, time zone: Asia/Ulaanbaatar
OS name: "linux", version: "7.2.11-arch1-1", arch: "amd64", family: "unix"
```

Репозиторийг татаж, тест ажиллуулах командууд:

```bash
git clone https://github.com/Tontoosh/phchanarlab8.git
cd phchanarlab8
mvn test
```

**Хэсэг A — тестүүд.** Тестийн класс: [CourseRegistrationServiceTest.java](src/test/java/mn/edu/must/sqat/CourseRegistrationServiceTest.java). `@ExtendWith(MockitoExtension.class)` болон хоёр `@Mock` ашиглаж, `@BeforeEach` дотор үйлчилгээг mock хамаарлуудтай нь шинээр үүсгэсэн.

| Тест | Шалгасан нөхцөл, хүлээгдсэн үр дүн | Ашигласан хэрэгсэл |
|---|---|---|
| A1 | Идэвхтэй, өмнөх холбоо хичээлээ үзсэн оюутанд `OK`; бүртгэл хадгалах, и-мэйл илгээх дуудлага хийгдэнэ. | `when`, `verify` |
| A2 | Оюутан олдохгүй бол `ERROR_NO_STUDENT`; хадгалалт, и-мэйл илгээлт хийгдэхгүй. | `when`, `never` |
| A3 | Идэвхгүй оюутанд `ERROR_INACTIVE_STUDENT`; хадгалалт хийгдэхгүй, и-мэйл илгээгчтэй огт харьцахгүй. | `when`, `never`, `verifyNoInteractions` |
| A4 | Өмнөх холбоо хичээл дутуу бол `ERROR_PREREQUISITES`; хадгалалт, и-мэйл илгээлт хийгдэхгүй. | `when`, `never`, `verifyNoInteractions` |
| A5 | И-мэйл илгээгч `RuntimeException` шидсэн ч `OK` буцаж, бүртгэл хадгалах дуудлага хийгдэнэ. | `doThrow`, `verify` |
| A6 | `studentId`, `courseId` тус бүр болон хоёул `null` үед `IllegalArgumentException`; хоёр хамааралтай огт харьцахгүй. | `assertThrows`, `verifyNoInteractions` |
| A7 | Илгээгдсэн и-мэйлийн биед хичээлийн код орсон байна. | `ArgumentCaptor`, `assertTrue` |
| A8 | Оюутан олдохгүй, оюутан идэвхгүй, хичээл олдохгүй гэсэн гурван алдааны ангийг шалгана. | `@ParameterizedTest`, `@CsvSource`, `lenient`, `never` |

Нийт **10 тест** ажилласан: A1–A7-ийн 7 энгийн тест, A8-ийн 3 параметржүүлсэн мөр. A6-ийн бүх `null` шалгалт нэг тестийн метод дотор байрладаг. Matcher хэрэглэсэн `send()` дуудлагын бүх аргументад `eq`, `anyString`, `contains` эсвэл `capture` зэрэг matcher хэрэглэсэн.

**A8-ийн `lenient()` тайлбар.** A8-ийн эхний хоёр мөрд оюутан олдохгүй эсвэл идэвхгүй тул `register()` нь `courseExists()`-ийг дуудахаас өмнө буцдаг. Иймээс зөвхөн `courseExists()`-ийн stub-д `lenient()` хэрэглэж, ашиглагдаагүй stub-ээс үүсэх `UnnecessaryStubbingException`-ээс сэргийлсэн бөгөөд бусад stub strict шалгалттай хэвээр байна.

Эх кодыг сэргээсний дараах эцсийн гаралт [results/mvn-test.txt](results/mvn-test.txt) файлд хадгалагдсан. Файлын сүүлчийн нийт дүн:

```text
Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Гаралтыг файлд хадгалах команд:

```bash
mkdir -p results
mvn test 2>&1 | tee results/mvn-test.txt
```

**Хэсэг B — мини мутаци.** Мутаци бүрийг тус тусад нь хийж, тестүүдийг ажиллуулсны дараа эх кодыг сэргээсэн. Дараах хүснэгтийн тоонууд `Tests run / Failures / Errors / Skipped` дараалалтай бөгөөд гурван мутантын лог бүрд `BUILD FAILURE` гарсан.

| Мутант | Түр хийсэн өөрчлөлт | Унасан тестүүд | Тестийн тоонууд | Нотолгоо |
|---|---|---|---|---|
| 1 | `if (!student.active()) ...` шалгалтыг комментлосон. | A3, A8-ийн 2-р мөр (`ERROR_INACTIVE_STUDENT`). | `10 / 2 / 0 / 0` | [mvn-test-mutant-1.txt](results/mvn-test-mutant-1.txt) |
| 2 | `repository.saveRegistration(studentId, courseId)` дуудалтыг комментлосон. | A1, A5. | `10 / 2 / 0 / 0` | [mvn-test-mutant-2.txt](results/mvn-test-mutant-2.txt) |
| 3 | Эхний `if`-ээс `\|\| courseId == null` хэсгийг авч, зөвхөн `studentId`-г шалгадаг болгосон. | A6. | `10 / 1 / 0 / 0` | [mvn-test-mutant-3.txt](results/mvn-test-mutant-3.txt) |

Эхний мутантын үед A3 нь `ERROR_INACTIVE_STUDENT` хүлээсэн боловч `ERROR_NO_COURSE`, A8-ийн 2-р мөр нь `ERROR_INACTIVE_STUDENT` хүлээсэн боловч `OK` авсан. Хоёр дахь мутантын үед A1, A5-ийн `verify(repository).saveRegistration(...)` шалгалтууд хадгалах дуудлага хийгдээгүйг `Wanted but not invoked` алдаагаар илрүүлсэн. Гурав дахь мутантын үед A6-ийн `courseId = null` шалгалт хүлээсэн `IllegalArgumentException` шидэгдэхгүй байгааг илрүүлсэн.

A6 нь анхнаасаа аргумент тус бүрийн `null` утгыг тусад нь шалгаж байсан тул гурав дахь мутантыг илрүүлэхийн тулд тестийг өөрчлөх шаардлага гараагүй. Сонгосон гурван эвдрэлийг бүгдийг илрүүлсэн нь идэвхгүй оюутны шалгалт, бүртгэл хадгалах дуудлага, `courseId`-ийн `null` шалгалтыг тестүүд хянаж байгааг харуулсан. Бүх мутацийн дараа `src/main`-ийг эх хувилбарт нь сэргээж, эцсийн гүйлтээр 10 тест бүгд амжилттай ажилласан.

**Дүгнэлт.** Mockito mock нь хамаарлын буцаах утга, шидэх алдаа болон дуудалтыг тест бүрд тохируулж, үйлчилгээний логикийг тусгаарлан шалгах боломж олгодог. Гараар бичсэн fake нь интерфейсийн хялбаршуулсан боловч ажилладаг хэрэгжүүлэлт бөгөөд өөрийн төлөв, логиктой байдаг. Энэ лабораторид `StudentRepository`, `EmailSender`-ийг mock болгосноор бодит сан, SMTP сервер шаардаагүй. Тестлэж буй `CourseRegistrationService`-ийг mock хийх ёсгүй бөгөөд `Student`, жагсаалт зэрэг энгийн өгөгдлийн объектыг бодитоор ашиглах нь шалгах логикийг хадгалдаг. `verify`-г бүх дотоод дуудалтад хэрэглэх нь тестийг хэрэгжүүлэлтийн нарийн бүтэцтэй хэт холбож, зөв refactor хийхэд ч унах эрсдэлтэй. Иймээс хадгалалт, мэдэгдэл зэрэг гэрээнд заасан чухал үйлдлийг `verify` хийж, буцаах үр дүнг assertion-аар шалгах нь зохистой. Гурван мутаци нь идэвхгүй оюутны шалгалт, хадгалалт, `courseId`-ийн `null` шалгалт алдагдахад тестүүд унаж байгааг бодитоор баталсан. Эх кодыг сэргээсний дараах 10 амжилттай тест нь эцсийн хувилбар шалгасан тохиолдлуудын хүлээгдсэн үр дүнг хангаж байгааг харуулсан.
