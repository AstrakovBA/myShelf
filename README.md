<<<<<<< HEAD
# [Название курсового проекта]

**Автор:** [ФИО]  
**Группа:** [номер]  
**Траектория:** [Desktop / Web / Mobile / Enterprise]  
**Дата начала:** [ДД.ММ.ГГГГ]  
**Дата сдачи:** [ДД.ММ.ГГГГ]
```

## Описание проекта

[2-3 предложения о том, что делает система]

**Пример:**  
Система управления мероприятиями (Event Management System) — это веб-приложение для организации и проведения мероприятий. Позволяет организаторам создавать мероприятия, управлять регистрацией участников и формировать отчёты.

```
##  Траектория выполнения

- [x] **Веб-разработка** (React + Spring Boot)
- [ ] Десктоп
- [ ] Мобильная
- [ ] Enterprise

```

## Технологический стек

| Компонент       |            Технология              |
|-----------------|------------------------------------|
| Бэкенд          | Java 17, Spring Boot 3, PostgreSQL |
| Фронтенд        | React 18, TypeScript, Axios        |
| API             | REST, OpenAPI (Swagger)            |
| Безопасность    | JWT, BCrypt                        |
| Сборка          | Maven, Vite                        |
| Контейнеризация | Docker (опционально)               |
| Инструменты     | Git, Postman, JaCoCo, SonarQube    |

```

##  Требования к окружению

| Требование           | Версия |
|----------------------|--------|
| Java JDK             |   17+  |
| Node.js              |   18+  |
| PostgreSQL           |   15+  |
| Maven                |   3.8+ |
| Docker (опционально) |   20+  |



## Установка и запуск

### 1. Клонирование репозитория


```bash
git clone https://github.com/username/course-project.git
cd course-project

```

### 2. Запуск бэкенда

```bash
cd backend
./mvnw spring-boot:run


Сервер запустится на http://localhost:8080

Swagger UI: http://localhost:8080/swagger-ui.html

```
### 3. Запуск фронтенда

```bash
cd frontend
npm install
npm run dev


Приложение откроется на http://localhost:5173
```
 ### 4. Запуск через Docker (опциональной) 
 
```bash
docker-compose up -d

```
 ### 5. API Endpoints

Базовый URL: http://localhost:8080/api


|  Метод |         Эндпоинт      | Описание                   |    Доступ   |
|--------|-----------------------|----------------------------|-------------|
| POST   | /auth/login           | Вход в систему             | Публичный   |
| POST   | /auth/register        | Регистрация пользователя   | Публичный   |
| GET    | /events               | Список мероприятий         | USER, ADMIN |
| GET    | /events/{id}          | Детали мероприятия         | USER, ADMIN |
| POST   | /events               | Создание мероприятия       | ADMIN       |
| PUT    | /events/{id}          | Обновление мероприятия     | ADMIN       |
| DELETE | /events/{id}          | Удаление мероприятия       | ADMIN       |
| POST   | /events/{id}/register | Регистрация на мероприятие | USER        |
| GET    | /users/me             | Профиль пользователя       | USER        |

Полная документация API: [Swagger UI](http://localhost:8080/swagger-ui.html)  
Postman коллекция: docs/09-api/postman-collection.json



### 6. Структура документации

Вся документация находится в папке [docs/](docs/):


 [00-project-charter/](docs/00-project-charter/) 
| Паспорт проекта, IDEF0, BUC, SWOT, ROI   | 
| [01-requirements/](docs/01-requirements/)       
| Use Case, Domain Model, трассировка      | 
| [02-architecture/](docs/02-architecture/)       
| PCMEF, ADR, интерфейсы                   | 
| [03-database/](docs/03-database/)               
| ER-диаграмма, DDL, ORM                   | 
| [04-detailed-design/](docs/04-detailed-design/) 
| Sequence диаграммы, спецификация методов | 
| [05-implementation/](docs/05-implementation/)  
| Реализация слоёв                         | 
| [06-testing/](docs/06-testing/)                
| Тест-планы, JaCoCo, Postman              | 
| [07-refactoring/](docs/07-refactoring/)        
| «Запахи кода», Data Mapper, Identity Map | 
| [08-ui/](docs/08-ui/)                          
| Скриншоты интерфейсов                    | 
| [09-api/](docs/09-api/)                        
| OpenAPI, Swagger                         | 
| [10-deployment/](docs/10-deployment/)          
| Docker, CI/CD, администрирование         | 
| [11-user-guide/](docs/11-user-guide/)          
| Руководство пользователя                 | 
| [12-final-report/](docs/12-final-report/)       
| Пояснительная записка, презентация       | 



 ### 7.Архитектура (PCMEF)

Система построена на архитектурном паттерне PCMEF (Presentation-Control-Mediator-Entity-Foundation).

Распределение слоёв:

|       Слой       |    Расположение |     Ответственность          |
|------------------|-----------------|------------------------------|
| Presentation (P) | React (браузер) | UI, отображение, ввод данных |
| Control (C)      | Spring Boot     | REST API, валидация DTO      |
| Mediator (M)     | Spring Boot     | Бизнес-логика, транзакции    |
| Entity (E)       | Spring Boot     | JPA-сущности                 |
| Foundation (F)   | Spring Boot     | Репозитории, доступ к БД     |

![Диаграмма пакетов PCMEF](docs/02-architecture/diagrams/package-diagram.png)

Ключевые ADR:  
- [ADR-001: Выбор архитектурного паттерна](docs/02-architecture/adr/adr-001.md)  
- [ADR-002: Выбор базы данных и ORM](docs/02-architecture/adr/adr-002.md)  
- [ADR-003: Стратегия аутентификации](docs/02-architecture/adr/adr-003.md)

---

 ### 8. Статистика разработки

 Git метрики

|          Метрика |                  Значение        |
|---------------------------|-------------------------|
| Всего коммитов            | 47                      |
| Период разработки         | 01.03.2026 – 30.05.2026 |
| Средняя частота           | 2.9 коммита/неделю      |
| Покрытие тестами (JaCoCo) | 42%                     |

 График активности

![Commit Activity](docs/images/git-commit-activity.png)

Рисунок 1 — Активность коммитов в течение семестра

 Тепловая карта

![Punch Card](docs/images/git-punch-card.png)

Рисунок 2 — Распределение коммитов по дням и часам



 ### 9. Авторы

- [Фамилия Имя] — разработчик, документация  
  Группа [номер], email: [email], GitHub: [username]



 ## Лицензия

MIT License
Этот проект распространяется под лицензией MIT. Подробности в файле [LICENSE](LICENSE).




 🔗 Полезные ссылки

- [Репозиторий проекта](https://github.com/username/course-project)
- [Документация (docs/)](docs/)
- [Swagger UI](http://localhost:8080/swagger-ui.html)
- [Postman коллекция](docs/09-api/postman-collection.json)



=======
# [Название курсового проекта]

**Автор:** [ФИО]  
**Группа:** [номер]  
**Траектория:** [Desktop / Web / Mobile / Enterprise]  
**Дата начала:** [ДД.ММ.ГГГГ]  
**Дата сдачи:** [ДД.ММ.ГГГГ]


## Описание проекта

[2-3 предложения о том, что делает система]

**Пример:**  
Система управления мероприятиями (Event Management System) — это веб-приложение для организации и проведения мероприятий. Позволяет организаторам создавать мероприятия, управлять регистрацией участников и формировать отчёты.


##  Траектория выполнения

- [x] **Веб-разработка** (React + Spring Boot)
- [ ] Десктоп
- [ ] Мобильная
- [ ] Enterprise



## Технологический стек

| Компонент       |            Технология              |
|-----------------|------------------------------------|
| Бэкенд          | Java 17, Spring Boot 3, PostgreSQL |
| Фронтенд        | React 18, TypeScript, Axios        |
| API             | REST, OpenAPI (Swagger)            |
| Безопасность    | JWT, BCrypt                        |
| Сборка          | Maven, Vite                        |
| Контейнеризация | Docker (опционально)               |
| Инструменты     | Git, Postman, JaCoCo, SonarQube    |



##  Требования к окружению

| Требование           | Версия |
|----------------------|--------|
| Java JDK             |   17+  |
| Node.js              |   18+  |
| PostgreSQL           |   15+  |
| Maven                |   3.8+ |
| Docker (опционально) |   20+  |



## Установка и запуск

### 1. Клонирование репозитория

```bash
git clone https://github.com/username/course-project.git
cd course-project
>>>>>>> 600ac1be3a62f5ffb8a548f4fae21b4978cd2dd3
