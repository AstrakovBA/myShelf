# Развёртывание Spring Boot сервера с помощью Docker

Этот документ описывает процедуру развёртывания приложения **my-shelf-server** с использованием Docker и Docker Compose.

## Содержание

1. [Требования](#требования)
2. [Структура Docker конфигурации](#структура-docker-конфигурации)
3. [Быстрый старт](#быстрый-старт)
4. [Подробное описание процесса сборки](#подробное-описание-процесса-сборки)
5. [Переменные окружения](#переменные-окружения)
6. [Управление контейнерами](#управление-контейнерами)

## Требования

### Системные требования

- **Docker**: версия 20.10+ ([установка](https://docs.docker.com/get-docker/))
- **Docker Compose**: версия 1.29+ ([установка](https://docs.docker.com/compose/install/))
- **Дисковое пространство**: минимум 5 GB для образов и данных
- **Оперативная память**: рекомендуется минимум 2 GB

### Программные требования

Приложение использует следующие ключевые компоненты:

| Компонент | Версия | Назначение |
|-----------|--------|-----------|
| Java | 17 | Runtime для Spring Boot приложения |
| Maven | 3.8.1 | Сборка Java приложения |
| Spring Boot | 3.2.0 | Фреймворк приложения |
| PostgreSQL | 15 | База данных |
| Flyway | Latest | Миграции БД |
| JWT | 0.11.5 | Аутентификация |

## Структура Docker конфигурации

```
myShelf/
├── docker/
│   ├── Dockerfile           # Multi-stage сборка приложения
│   └── docker-compose.yml   # Конфигурация контейнеров и сервисов
└── docs/
    └── 10-deployment/
        └── README.md        # Этот файл
```

### Dockerfile (Multi-stage сборка)

```dockerfile
# Этап 1: Сборка (Builder)
FROM maven:3.8.1-openjdk-17 AS builder
WORKDIR /app
COPY ../my-shelf-server .
RUN mvn clean package -DskipTests

# Этап 2: Запуск (Runtime)
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Описание:**
- **Этап 1 (builder)**: Собирает Java приложение с помощью Maven. Использует полный образ с JDK.
- **Этап 2 (runtime)**: Использует минимальный образ только с JRE, копирует скомпилированный JAR из этапа 1.
- **Результат**: Компактный образ только с необходимым для запуска (уменьшает размер образа с ~500MB до ~150MB).

### docker-compose.yml

Определяет два сервиса с общей сетью и томом для персистентности данных.

## Быстрый старт

### 1. Клонирование репозитория

```bash
git clone https://github.com/AstrakovBA/myShelf.git
cd myShelf
```

### 2. Запуск контейнеров

```bash
cd docker
docker-compose up -d
```

**Что происходит:**
- `-d` флаг запускает контейнеры в фоновом режиме (detached mode)
- Создаётся сеть `myshelf-network`
- Создаётся том `postgres_data` для сохранения данных
- Spring Boot приложение начнёт доступно на `http://localhost:8080`
- PostgreSQL доступна на `localhost:5432`

### 3. Проверка статуса

```bash
docker-compose ps
```

Ожидаемый вывод:
```
NAME                  COMMAND                  SERVICE             STATUS
postgres              "docker-entrypoint.s…"   postgres            Up X seconds
my-shelf-server       "java -jar app.jar"      my-shelf-server     Up X seconds
```

### 4. Доступ к приложению

- **REST API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **База данных**: `postgresql://postgres:password@localhost:5432/myshelf`

### 5. Просмотр логов

```bash
# Логи всех сервисов
docker-compose logs -f

# Логи конкретного сервиса
docker-compose logs -f my-shelf-server
docker-compose logs -f postgres
```

## Подробное описание процесса сборки

### Инициализация

При запуске контейнеров выполняются следующие этапы:

#### 1. Создание инфраструктуры Docker

```bash
docker-compose up -d
```

**Действия:**
- Docker создаёт образы на основе Dockerfile и официальных образов
- Создаёт контейнеры для each сервиса
- Создаёт сеть и том
- Запускает контейнеры в порядке: PostgreSQL → Spring Boot (из-за `depends_on`)

#### 2. Инициализация PostgreSQL (~ 2-5 сек)

```sql
CREATE DATABASE myshelf;
-- Таблицы будут созданы при запуске приложения
```

#### 3. Запуск Spring Boot (~ 10-30 сек)

```
- JVM инициализация
- Загрузка Spring Context
- Подключение к БД
- Выполнение Flyway миграций
- Запуск приложения на порту 8080
```

### Сборка образа приложения (при первом запуске)

Процесс занимает **5-15 минут** в зависимости от интернета и машины:

```bash
# 1. Maven скачивает зависимости (~200 MB)
#    - spring-boot-starters
#    - postgresql driver
#    - JWT библиотеки
#    - Swagger/OpenAPI
#    - Flyway для миграций

# 2. Компилирование кода Java
mvn clean package -DskipTests

# 3. Создание JAR файла
# /app/target/my-shelf-server-0.0.1-SNAPSHOT.jar (~30-50 MB)

# 4. Multi-stage сборка: копирование JAR в финальный образ
COPY --from=builder /app/target/*.jar app.jar
```

### Кэширование (последующие запуски)

После первой сборки Docker кэширует слои образа:
- Последующие запуски занимают **< 5 сек** для контейнеров
- Пересборка только если изменился код или зависимости

## Переменные окружения

### Конфигурация PostgreSQL

```yaml
services:
  postgres:
    environment:
      - POSTGRES_DB=myshelf           # Название БД
      - POSTGRES_PASSWORD=password    # Пароль (для разработки)
```

### Конфигурация Spring Boot

```yaml
services:
  my-shelf-server:
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/myshelf
      - SPRING_DATASOURCE_USERNAME=postgres
      - SPRING_DATASOURCE_PASSWORD=password
```

### Переопределение переменных

#### Для разработки (локально)

Создайте файл `.env` в папке `docker/`:

```bash
# docker/.env
POSTGRES_PASSWORD=secure_dev_password
SPRING_PROFILE=dev
```

Используйте в `docker-compose.yml`:

```yaml
services:
  postgres:
    environment:
      - POSTGRES_PASSWORD=${POSTGRES_PASSWORD:-password}
```

Затем запустите:

```bash
docker-compose --env-file .env up -d
```

#### Для production (безопасное хранение)

Используйте Docker Secrets или переменные окружения:

```bash
export POSTGRES_PASSWORD=super_secure_password_here
docker-compose up -d
```

## Управление контейнерами

### Основные команды

```bash
# Запуск контейнеров
docker-compose up -d

# Остановка контейнеров (с сохранением данных)
docker-compose stop

# Полное удаление контейнеров (данные в томе сохраняются)
docker-compose down

# Удаление контейнеров И всех данных (включая БД)
docker-compose down -v

# Перезапуск контейнеров
docker-compose restart

# Пересборка образов после изменений кода
docker-compose up -d --build

# Пересборка без кэша
docker-compose up -d --build --no-cache
```

### Просмотр ресурсов

```bash
# Статус контейнеров
docker-compose ps

# Логи (все сервисы, следование за новыми)
docker-compose logs -f

# Логи конкретного сервиса, последние 100 строк
docker-compose logs --tail 100 my-shelf-server

# Использование ресурсов
docker stats

# Просмотр томов
docker volume ls

# Информация об образах
docker images
```

### Взаимодействие с контейнерами

```bash
# Подключение к контейнеру (bash/shell)
docker-compose exec my-shelf-server /bin/bash
docker-compose exec postgres bash

# Выполнение команды в контейнере
docker-compose exec my-shelf-server java -version

# Проверка конфигурации БД
docker-compose exec postgres psql -U postgres -d myshelf -c "\dt"

# Просмотр переменных окружения контейнера
docker-compose config
```
