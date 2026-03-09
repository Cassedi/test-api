# CRM REST API

Учебное CRM-приложение для тренировки автоматизации тестирования REST API.

## Стек технологий

- Java 21
- Spring Boot 4.0
- Spring Data JPA + Hibernate
- PostgreSQL 17
- Flyway (миграции БД)
- Lombok
- Bean Validation (Jakarta Validation)
- SpringDoc OpenAPI (Swagger UI)
- JUnit 5 + Mockito
- Docker + Docker Compose

## Быстрый старт

### Запуск через Docker Compose

```bash
docker-compose up --build
```

API будет доступен на `http://localhost:8080`.

### Swagger UI

После запуска откройте в браузере:

```
http://localhost:8080/swagger-ui/index.html
```

### Остановка

```bash
docker-compose down
```

Для полной очистки данных (удаление volume с PostgreSQL):

```bash
docker-compose down -v
```

## Структура проекта

```
src/main/java/org/example/testapi/
├── config/
│   └── FlywayConfig.java           # Конфигурация Flyway миграций
├── controller/
│   ├── ClientController.java        # CRUD эндпоинты для клиентов
│   ├── ProviderController.java      # CRUD эндпоинты для провайдеров
│   └── TaskController.java          # CRUD эндпоинты для задач
├── dto/
│   └── TaskRequest.java             # DTO для создания/обновления задач
├── exception/
│   └── GlobalExceptionHandler.java  # Обработка ошибок (422, 404)
├── model/
│   ├── Client.java                  # JPA-сущность клиента
│   ├── Provider.java                # JPA-сущность провайдера
│   └── Task.java                    # JPA-сущность задачи
├── repository/
│   ├── ClientRepository.java
│   ├── ProviderRepository.java
│   └── TaskRepository.java
├── service/
│   ├── ClientService.java
│   ├── ProviderService.java
│   └── TaskService.java
└── TestApiApplication.java          # Точка входа

src/main/resources/
├── application.yaml                 # Конфигурация приложения
└── db/migration/
    ├── V1__create_tables.sql        # Создание таблиц
    └── V2__seed_data.sql            # Тестовые данные
```

## Модели данных

### Client (Клиент)

| Поле      | Тип            | Валидация                  |
|-----------|----------------|----------------------------|
| id        | Long           | Auto-generated             |
| name      | String         | Обязательное               |
| email     | String         | Валидный email             |
| phone     | String         | Формат: +7-15 цифр         |
| company   | String         | Опциональное               |
| createdAt | LocalDateTime  | Автоматически              |

### Provider (Провайдер)

| Поле        | Тип            | Валидация                |
|-------------|----------------|--------------------------|
| id          | Long           | Auto-generated           |
| name        | String         | Обязательное             |
| email       | String         | Валидный email           |
| phone       | String         | Формат: +7-15 цифр       |
| serviceType | String         | Обязательное             |
| createdAt   | LocalDateTime  | Автоматически            |

### Task (Задача)

| Поле        | Тип      | Валидация              |
|-------------|----------|------------------------|
| id          | Long     | Auto-generated         |
| title       | String   | Обязательное           |
| description | String   | Опциональное           |
| status      | String   | По умолчанию "NEW"     |
| client      | Client   | Обязательное (clientId) |
| provider    | Provider | Опциональное (providerId) |

### Связи между сущностями

- **Client → Task**: один ко многим. При удалении клиента все его задачи удаляются каскадно.
- **Provider → Task**: один ко многим. При удалении провайдера у задач поле `provider` устанавливается в `null`.

## API эндпоинты

### Clients — `/api/clients`

| Метод    | URL                | Описание               | Код ответа |
|----------|--------------------|------------------------|------------|
| `GET`    | `/api/clients`     | Список всех клиентов   | 200        |
| `GET`    | `/api/clients/{id}`| Клиент по ID           | 200 / 404  |
| `POST`   | `/api/clients`     | Создать клиента        | 201 / 422  |
| `PUT`    | `/api/clients/{id}`| Обновить клиента       | 200 / 422  |
| `DELETE` | `/api/clients/{id}`| Удалить клиента        | 204 / 404  |

### Providers — `/api/providers`

| Метод    | URL                  | Описание                | Код ответа |
|----------|----------------------|-------------------------|------------|
| `GET`    | `/api/providers`     | Список всех провайдеров | 200        |
| `GET`    | `/api/providers/{id}`| Провайдер по ID         | 200 / 404  |
| `POST`   | `/api/providers`     | Создать провайдера      | 201 / 422  |
| `PUT`    | `/api/providers/{id}`| Обновить провайдера     | 200 / 422  |
| `DELETE` | `/api/providers/{id}`| Удалить провайдера      | 204 / 404  |

### Tasks — `/api/tasks`

| Метод    | URL               | Описание             | Код ответа |
|----------|--------------------|----------------------|------------|
| `GET`    | `/api/tasks`       | Список всех задач    | 200        |
| `GET`    | `/api/tasks/{id}`  | Задача по ID         | 200 / 404  |
| `POST`   | `/api/tasks`       | Создать задачу       | 201 / 422  |
| `PUT`    | `/api/tasks/{id}`  | Обновить задачу      | 200 / 422  |
| `DELETE` | `/api/tasks/{id}`  | Удалить задачу       | 204 / 404  |

## Примеры запросов

### Создание клиента

```bash
curl -X POST http://localhost:8080/api/clients \
  -H "Content-Type: application/json" \
  -d '{"name": "John Doe", "email": "john@example.com", "phone": "+1234567890", "company": "Acme Corp"}'
```

### Создание задачи

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title": "Setup infrastructure", "description": "Configure AWS", "status": "NEW", "clientId": 1, "providerId": 1}'
```

### Создание провайдера

```bash
curl -X POST http://localhost:8080/api/providers \
  -H "Content-Type: application/json" \
  -d '{"name": "Cloud Provider", "email": "cloud@example.com", "phone": "+1111111111", "serviceType": "CLOUD"}'
```

## Формат ошибок

Все ошибки возвращаются в едином формате:

```json
{
    "timestamp": 1710417124782,
    "status": 422,
    "error": "Unprocessable Entity",
    "message": "Field 'name' is required",
    "path": "/api/clients"
}
```

| Статус | Когда                                    |
|--------|------------------------------------------|
| 404    | Сущность не найдена по ID                |
| 422    | Ошибка валидации полей                   |

## Тестовые данные

При первом запуске Flyway создаёт таблицы и наполняет их данными:

**Клиенты:** John Doe (Acme Corp), Jane Smith (Tech Solutions), Bob Wilson (Global Services)

**Провайдеры:** Cloud Provider (CLOUD), Security Solutions (SECURITY), Data Analytics Co (ANALYTICS)

**Задачи:** 5 задач со статусами NEW, IN_PROGRESS, DONE, привязанные к клиентам и провайдерам.

## Тесты

Запуск тестов:

```bash
./mvnw test
```

Тесты контроллеров используют `@WebMvcTest` + `@MockitoBean` и проверяют:

- CRUD-операции (GET, POST, PUT, DELETE)
- Корректные статус-коды (200, 201, 204, 404, 422)
- Формат JSON-ответов
- Валидацию полей
- Формат ошибок

## Конфигурация

Приложение настраивается через переменные окружения:

| Переменная    | По умолчанию | Описание          |
|---------------|-------------|-------------------|
| `DB_HOST`     | localhost   | Хост PostgreSQL   |
| `DB_PORT`     | 5432        | Порт PostgreSQL   |
| `DB_NAME`     | crm_db      | Имя базы данных   |
| `DB_USER`     | postgres    | Пользователь БД   |
| `DB_PASSWORD` | postgres    | Пароль БД         |
