# Развёртывание Spring Boot сервера с помощью Docker

Этот документ описывает процедуру развёртывания приложения **my-shelf-server** с использованием Docker и Docker Compose.

## 📋 Содержание

1. [Требования](#требования)
2. [Архитектура](#архитектура)
3. [Структура Docker конфигурации](#структура-docker-конфигурации)
4. [Быстрый старт](#быстрый-старт)
5. [Подробное описание процесса сборки](#подробное-описание-процесса-сборки)
6. [Переменные окружения](#переменные-окружения)
7. [Управление контейнерами](#управление-контейнерами)
8. [Решение проблем](#решение-проблем)
9. [Бэкапы и восстановление](#бэкапы-и-восстановление)

## 📦 Требования

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

## 🏗️ Архитектура

Развёртывание состоит из двух контейнеров, объединённых в одну сеть:

```
┌─────────────────────────────────────────────────────────┐
│           Docker Network (myshelf-network)              │
├─────────────────────┬─────────────────────────────────┤
│                     │                                  │
│  ┌────────────────┐ │  ┌──────────────────────────┐  │
│  │  PostgreSQL 15 │ │  │  Spring Boot (Port 8080) │  │
│  │  (Port 5432)   │◄─►  │  (my-shelf-server)       │  │
│  │                │ │  │                          │  │
│  │ - Database:    │ │  │ - Java 17 Runtime        │  │
│  │   myshelf      │ │  │ - Maven build            │  │
│  │ - Data Volume: │ │  │ - Swagger UI             │  │
│  │   postgres_data│ │  │ - Rest API               │  │
│  └────────────────┘ │  └──────────────────────────┘  │
│                     │                                  │
└─────────────────────┴─────────────────────────────────┘
         ↑
         │ (порты маршрутизированы на хост)
         │
    Хост система
```

## 📁 Структура Docker конфигурации

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

## 🚀 Быстрый старт

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
- `-d` флаг запускает контейнеры в фонов��м режиме (detached mode)
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

## 🔧 Подробное описание процесса сборки

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

## 🌍 Переменные окружения

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

## 🎮 Управление контейнерами

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

## 🔍 Решение проблем

### 1. Spring Boot контейнер не запускается

**Симптом:**
```
my-shelf-server    "java -jar app.jar"    exited with code 1
```

**Решение:**

```bash
# 1. Просмотр полных логов
docker-compose logs my-shelf-server

# 2. Проверка статуса PostgreSQL
docker-compose logs postgres

# 3. Убедитесь, что PostgreSQL запустился и готов
# Ождите 5-10 секунд после docker-compose up

# 4. Если ошибка "Cannot connect to PostgreSQL"
#    Перезапустите оба контейнера
docker-compose restart
```

### 2. Порт 8080 уже занят

**Симптом:**
```
Error: bind: address already in use
```

**Решение:**

```bash
# Вариант 1: Остановить другой процесс
lsof -i :8080  # на macOS/Linux
netstat -ano | findstr :8080  # на Windows

# Вариант 2: Использовать другой порт
# Отредактируйте docker-compose.yml
services:
  my-shelf-server:
    ports:
      - "9090:8080"  # Хост:Контейнер

docker-compose up -d
```

### 3. PostgreSQL контейнер не инициализируется

**Симптом:**
```
postgres    "docker-entrypoint.s…"    restarting
```

**Решение:**

```bash
# 1. Удалите повреждённый том
docker-compose down -v

# 2. Пересоздайте контейнеры
docker-compose up -d

# 3. Если проблема сохраняется, проверьте место на диске
df -h  # Должно быть минимум 1 GB свободного места
```

### 4. Недостаточно памяти

**Симптом:**
```
OutOfMemoryError: Java heap space
```

**Решение:**

Отредактируйте `docker-compose.yml`:

```yaml
services:
  my-shelf-server:
    environment:
      - _JAVA_OPTIONS=-Xmx512m -Xms256m
```

или в переменные окружения контейнера:

```bash
docker-compose exec my-shelf-server -e _JAVA_OPTIONS="-Xmx1g" up -d
```

### 5. Миграции БД не применяются

**Решение:**

```bash
# Проверьте логи миграций
docker-compose logs my-shelf-server | grep -i flyway

# Проверьте содержимое папки миграций
docker-compose exec postgres psql -U postgres -d myshelf \
  -c "SELECT * FROM flyway_schema_history;"

# Если нужно сбросить миграции (только для разработки)
docker-compose down -v
docker-compose up -d
```

## 💾 Бэкапы и восстановление

### Бэкап БД

#### Создание дампа PostgreSQL

```bash
# Полный дамп БД
docker-compose exec postgres pg_dump -U postgres -d myshelf > backup_myshelf_$(date +%Y%m%d_%H%M%S).sql

# Сжатый дамп
docker-compose exec postgres pg_dump -U postgres -d myshelf -Fc > backup_myshelf_$(date +%Y%m%d_%H%M%S).dump

# Дамп только структуры (без данных)
docker-compose exec postgres pg_dump -U postgres -d myshelf --schema-only > backup_schema.sql
```

#### Резервная копия данных (тома Docker)

```bash
# Архивирование тома PostgreSQL
docker run --rm \
  -v myshelf_postgres_data:/data \
  -v $(pwd):/backup \
  alpine tar czf /backup/postgres_backup_$(date +%Y%m%d_%H%M%S).tar.gz -C /data .
```

### Восстановление БД

#### Из SQL дампа

```bash
# Восстановление на работающей системе
docker-compose exec postgres psql -U postgres -d myshelf < backup_myshelf_20240101_120000.sql

# Восстановление из сжатого дампа
docker-compose exec postgres pg_restore -U postgres -d myshelf backup_myshelf.dump
```

#### Из архива тома

```bash
# 1. Остановить контейнеры
docker-compose stop

# 2. Удалить текущий том
docker volume rm myshelf_postgres_data

# 3. Создать новый том и восстановить из архива
docker run --rm \
  -v myshelf_postgres_data:/data \
  -v $(pwd):/backup \
  alpine tar xzf /backup/postgres_backup_20240101_120000.tar.gz -C /data

# 4. Запустить контейнеры
docker-compose up -d
```

### Автоматизация бэкапов

Создайте скрипт `backup.sh`:

```bash
#!/bin/bash

BACKUP_DIR="./backups"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
DB_NAME="myshelf"

mkdir -p $BACKUP_DIR

# Создание дампа
docker-compose exec postgres pg_dump \
  -U postgres \
  -d $DB_NAME \
  -Fc > $BACKUP_DIR/backup_${DB_NAME}_${TIMESTAMP}.dump

echo "Backup created: $BACKUP_DIR/backup_${DB_NAME}_${TIMESTAMP}.dump"

# Удаление старых бэкапов (старше 30 дней)
find $BACKUP_DIR -type f -name "backup_*.dump" -mtime +30 -delete

echo "Old backups cleaned up"
```

Запустите периодически через cron:

```bash
# Каждый день в 2:00 AM
0 2 * * * cd /path/to/myShelf/docker && ./backup.sh
```

## 📊 Мониторинг и логирование

### Просмотр логов в реальном времени

```bash
# Все логи с 100 последними строками
docker-compose logs --tail 100 -f

# Логи конкретного сервиса за последний час
docker-compose logs --since 1h my-shelf-server

# Только ошибки
docker-compose logs my-shelf-server | grep -i error
```

### Проверка здоровья приложения

```bash
# Проверка доступности API
curl -s http://localhost:8080/swagger-ui.html

# Проверка здоровья Spring Boot актуатора (если включен)
curl -s http://localhost:8080/actuator/health

# Проверка БД
docker-compose exec postgres psql -U postgres -d myshelf -c "SELECT version();"
```

### Сбор статистики

```bash
# CPU, память, дисковые операции
docker stats

# История использования памяти
docker-compose exec my-shelf-server cat /proc/meminfo

# Размер логов контейнера
docker exec my-shelf-server du -sh /var/log/
```

## 🔐 Безопасность для Production

### Хорошие практики

1. **Используйте .env для чувствительных данных**

```bash
# .env (добавьте в .gitignore)
POSTGRES_PASSWORD=SUPER_SECURE_PASSWORD_MIN_32_CHARS_LONG
JWT_SECRET=ANOTHER_SUPER_SECRET_KEY_MIN_32_CHARS
SPRING_PROFILE=production
```

2. **Используйте Docker Secrets (для Swarm)**

```bash
echo "super_secure_password" | docker secret create db_password -
```

3. **Ограничьте ресурсы контейнеров**

```yaml
services:
  my-shelf-server:
    deploy:
      resources:
        limits:
          cpus: '1'
          memory: 512M
        reservations:
          cpus: '0.5'
          memory: 256M
```

4. **Используйте Read-only filesystem где возможно**

```yaml
services:
  my-shelf-server:
    read_only: true
    tmpfs:
      - /tmp
```

5. **Регулярно обновляйте базовые образы**

```bash
# Проверка обновлений
docker-compose pull

# Пересборка с новыми образами
docker-compose up -d --build --pull always
```

## 📚 Полезные ресурсы

- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Reference](https://docs.docker.com/compose/compose-file/)
- [Spring Boot Docker](https://spring.io/guides/gs/spring-boot-docker/)
- [PostgreSQL Docker Hub](https://hub.docker.com/_/postgres)
- [Java in Docker Best Practices](https://github.com/GoogleContainerTools/distroless)

---

**Последнее обновление**: 2026-06-11

Для вопросов или проблем создайте [Issue](https://github.com/AstrakovBA/myShelf/issues) в репозитории.
