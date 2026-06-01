# my-shelf-server

Spring Boot backend для приложения My Shelf (гардероб, образы, JWT-аутентификация).

## Code Quality

Статический анализ кода выполняется **Checkstyle** на основе [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html) (`google_checks.xml` как эталон) с проектным файлом [`checkstyle.xml`](checkstyle.xml).

### Кастомные правила

| Правило | Значение |
|---------|----------|
| Максимальная длина строки | 120 символов |
| JavaDoc | Обязателен для `public` методов и типов (с исключениями в `checkstyle-suppressions.xml`) |
| `System.out` / `System.out.println` | Запрещены — использовать SLF4J `Logger` |
| Цикломатическая сложность метода | не более 10 |

### Как запустить анализ

Из каталога `my-shelf-server` (нужны **JDK 17+** и Maven):

```bash
mvn -Pcheckstyle checkstyle:check
```

Только отчёт без привязки к фазе `validate` (плагин должен быть в `pom.xml`):

```bash
mvn checkstyle:check
```

При нарушениях сборка завершается с ошибкой (`failOnViolation: true`).

### Как интерпретировать отчёт

1. **Консоль** — краткий список файлов, строк и правил (`[ERROR] ...`).
2. **XML-отчёт** — полный отчёт: `target/checkstyle-result.xml`.

Структура записи в XML:

```xml
<error line="42" column="5" severity="error"
       message="..." source="com.puppycrawl.tools.checkstyle.checks...."/>
```

| Поле | Смысл |
|------|--------|
| `line` / `column` | Место в файле |
| `message` | Описание нарушения |
| `source` | Идентификатор правила Checkstyle |

Подсчёт числа нарушений (PowerShell):

```powershell
(Select-Xml -Path target/checkstyle-result.xml -XPath "//error").Count
```

Подсчёт в Linux/macOS:

```bash
grep -c "<error " target/checkstyle-result.xml
```

### Результат последнего анализа (базовая линия)

Проверка: Checkstyle **10.12.7**, профиль `checkstyle`, только `src/main/java` (тесты исключены).

| Показатель | Значение |
|------------|----------|
| Дата проверки | 01.06.2026 |
| Файлов с замечаниями | **0** |
| **Всего нарушений** | **0** |
| Сборка | `BUILD SUCCESS` (`mvn checkstyle:check`) |

Исключения для `dto`, `entity`, `repository` заданы в [`checkstyle-suppressions.xml`](checkstyle-suppressions.xml).

### Связанные документы

- [Паттерны персистентности](../docs/07-refactoring/patterns.md)
- [Тестирование (JaCoCo)](../docs/06-testing/README.md)
