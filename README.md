# CRM REST API

Учебное CRM-приложение для тренировки автоматизации тестирования REST API.

## Стек технологий

- Java 21
- Spring Boot 4.0
- Spring Data JPA + Hibernate
- Spring Security + JWT (JJWT 0.12.6)
- PostgreSQL 17
- Flyway (миграции БД, кастомный запуск через `FlywayConfig`)
- Lombok
- Bean Validation (Jakarta Validation)
- SpringDoc OpenAPI (Swagger UI)
- JUnit 5 + Mockito
- H2 (in-memory в тестовом профиле)
- Docker + Docker Compose

## Быстрый старт

### Запуск через Docker Compose

```bash
docker-compose up --build
```

API будет доступен на `http://localhost:8080`.

### Аутентификация

Эндпоинты `/api/clients`, `/api/providers` и `/api/tasks` защищены: нужен заголовок `Authorization: Bearer <accessToken>`. Публичные без токена: `/api/auth/**`, а также Swagger UI и OpenAPI (`/swagger-ui/**`, `/v3/api-docs/**`).

При старте `AdminInitializer` создаёт пользователя с ролью `ADMIN`, если такого email ещё нет. По умолчанию (см. `application.yaml`): email `admin@crm.local`, пароль `admin123`. Для продакшена задайте сильные `ADMIN_EMAIL` / `ADMIN_PASSWORD` и обязательно переопределите `JWT_SECRET`.

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
│   ├── AdminInitializer.java        # Создание администратора при старте
│   └── FlywayConfig.java            # Конфигурация Flyway миграций
├── controller/
│   ├── AuthController.java          # Регистрация, вход, обновление токенов
│   ├── ClientController.java        # CRUD эндпоинты для клиентов
│   ├── ProviderController.java      # CRUD эндпоинты для провайдеров
│   └── TaskController.java          # CRUD эндпоинты для задач
├── dto/
│   ├── AuthResponse.java
│   ├── LoginRequest.java
│   ├── RegisterRequest.java
│   └── TaskRequest.java             # DTO для создания/обновления задач
├── exception/
│   └── GlobalExceptionHandler.java  # Обработка ошибок (422, 404)
├── model/
│   ├── Client.java
│   ├── Provider.java
│   ├── Task.java
│   ├── User.java
│   └── Role.java
├── repository/
│   ├── ClientRepository.java
│   ├── ProviderRepository.java
│   ├── TaskRepository.java
│   └── UserRepository.java
├── security/
│   ├── CustomAccessDeniedHandler.java
│   ├── CustomAuthEntryPoint.java
│   ├── JwtAuthenticationFilter.java
│   ├── JwtService.java
│   └── SecurityConfig.java
├── service/
│   ├── AuthService.java
│   ├── ClientService.java
│   ├── ProviderService.java
│   └── TaskService.java
└── TestApiApplication.java          # Точка входа

src/main/resources/
├── application.yaml                 # Конфигурация приложения (JWT, admin, БД)
└── db/migration/
    ├── V1__create_tables.sql        # Таблицы clients, providers, tasks
    ├── V2__seed_data.sql            # Тестовые данные CRM
    └── V3__create_users_table.sql   # Таблица users
```

## Модели данных

### User (Пользователь)

| Поле      | Тип           | Описание                          |
|-----------|---------------|-----------------------------------|
| id        | Long          | Auto-generated                    |
| email     | String        | Уникальный логин                  |
| password  | String        | BCrypt-хеш                        |
| role      | Role          | ADMIN, MANAGER или SERVICE        |
| createdAt | LocalDateTime | Автоматически                     |

При **регистрации** (`POST /api/auth/register`) новому пользователю назначается роль **MANAGER**.

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

### Auth — `/api/auth` (публично)

| Метод  | URL                 | Описание                         | Код ответа        |
|--------|---------------------|----------------------------------|-------------------|
| `POST` | `/api/auth/register`| Регистрация                      | 201 / 409 / 422   |
| `POST` | `/api/auth/login`   | Вход, пара JWT в теле ответа     | 200 / 401 / 422   |
| `POST` | `/api/auth/refresh` | Обновление пары по refresh-токену | 200 / 400 / 401 |

Тело ответа при успешном входе / регистрации / refresh:

```json
{
    "accessToken": "eyJhbGciOi...",
    "refreshToken": "eyJhbGciOi..."
}
```

Тело запроса для refresh:

```json
{
    "refreshToken": "<refresh-токен>"
}
```

### Доступ к CRM по ролям

| Операция | Разрешённые роли   |
|----------|--------------------|
| `GET` на `/api/clients`, `/api/providers`, `/api/tasks` | SERVICE, MANAGER, ADMIN |
| `POST`, `PUT` на эти ресурсы                          | MANAGER, ADMIN          |
| `DELETE`                                              | ADMIN                   |

### Clients — `/api/clients`

| Метод    | URL                | Описание               | Код ответа        |
|----------|--------------------|------------------------|-------------------|
| `GET`    | `/api/clients`     | Список всех клиентов   | 200               |
| `GET`    | `/api/clients/{id}`| Клиент по ID           | 200 / 404         |
| `POST`   | `/api/clients`     | Создать клиента        | 201 / 422         |
| `PUT`    | `/api/clients/{id}`| Обновить клиента       | 200 / 422         |
| `DELETE` | `/api/clients/{id}`| Удалить клиента        | 204 / 404         |

### Providers — `/api/providers`

| Метод    | URL                  | Описание                | Код ответа        |
|----------|----------------------|-------------------------|-------------------|
| `GET`    | `/api/providers`     | Список всех провайдеров | 200               |
| `GET`    | `/api/providers/{id}`| Провайдер по ID         | 200 / 404         |
| `POST`   | `/api/providers`     | Создать провайдера      | 201 / 422         |
| `PUT`    | `/api/providers/{id}`| Обновить провайдера     | 200 / 422         |
| `DELETE` | `/api/providers/{id}`| Удалить провайдера      | 204 / 404         |

### Tasks — `/api/tasks`

| Метод    | URL               | Описание             | Код ответа        |
|----------|--------------------|----------------------|-------------------|
| `GET`    | `/api/tasks`       | Список всех задач    | 200               |
| `GET`    | `/api/tasks/{id}`  | Задача по ID         | 200 / 404         |
| `POST`   | `/api/tasks`       | Создать задачу       | 201 / 422         |
| `PUT`    | `/api/tasks/{id}`  | Обновить задачу      | 200 / 422         |
| `DELETE` | `/api/tasks/{id}`  | Удалить задачу       | 204 / 404         |

## Примеры запросов

### Вход и использование access-токена

```bash
# Получить токены (дефолтный админ из application.yaml)
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@crm.local","password":"admin123"}'

# Подставьте accessToken из ответа в переменную TOKEN, затем:
export TOKEN="<accessToken>"

curl -X GET http://localhost:8080/api/clients \
  -H "Authorization: Bearer $TOKEN"
```

### Создание клиента (нужны права MANAGER или ADMIN)

```bash
curl -X POST http://localhost:8080/api/clients \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"name": "John Doe", "email": "john@example.com", "phone": "+1234567890", "company": "Acme Corp"}'
```

### Создание задачи

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"title": "Setup infrastructure", "description": "Configure AWS", "status": "NEW", "clientId": 1, "providerId": 1}'
```

### Создание провайдера

```bash
curl -X POST http://localhost:8080/api/providers \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
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
| 401    | Нет или невалидный JWT                   |
| 403    | Недостаточно прав для операции           |
| 404    | Сущность не найдена по ID                |
| 422    | Ошибка валидации полей                   |

## Тестовые данные

При первом запуске Flyway применяет миграции: схема CRM (V1), начальные клиенты, провайдеры и задачи (V2), таблица пользователей (V3). Пользователь-администратор создаётся кодом приложения, не через seed SQL.

**Клиенты:** John Doe (Acme Corp), Jane Smith (Tech Solutions), Bob Wilson (Global Services)

**Провайдеры:** Cloud Provider (CLOUD), Security Solutions (SECURITY), Data Analytics Co (ANALYTICS)

**Задачи:** 5 задач со статусами NEW, IN_PROGRESS, DONE, привязанные к клиентам и провайдерам.

## Тесты

Запуск тестов:

```bash
./mvnw test
```

Тесты контроллеров используют `@WebMvcTest` + `@MockitoBean` и проверяют:

- CRUD-операции (GET, POST, PUT, DELETE) для клиентов, провайдеров и задач
- Аутентификацию: регистрация, вход, обновление токенов (`AuthControllerTest`)
- Корректные статус-коды (200, 201, 204, 400, 401, 404, 409, 422)
- Формат JSON-ответов
- Валидацию полей
- Формат ошибок

Для `@WebMvcTest` контроллеров CRM используется `@AutoConfigureMockMvc(addFilters = false)` и `@MockitoBean` для `JwtAuthenticationFilter`, чтобы не прогонять JWT-фильтр в юнит-тестах слоя контроллера.

## Конфигурация

Приложение настраивается через переменные окружения:

| Переменная       | По умолчанию (dev) | Описание                          |
|------------------|--------------------|-----------------------------------|
| `DB_HOST`        | localhost          | Хост PostgreSQL                   |
| `DB_PORT`        | 5432               | Порт PostgreSQL                   |
| `DB_NAME`        | crm_db             | Имя базы данных                   |
| `DB_USER`        | postgres           | Пользователь БД                   |
| `DB_PASSWORD`    | postgres           | Пароль БД                         |
| `JWT_SECRET`     | см. `application.yaml` | Секрет подписи JWT (в прод обязателен свой) |
| `ADMIN_EMAIL`    | admin@crm.local    | Email администратора при старте   |
| `ADMIN_PASSWORD` | admin123           | Пароль администратора при старте  |

Сроки жизни access- и refresh-токенов задаются в `application.yaml` (`jwt.access-token-expiration`, `jwt.refresh-token-expiration`, в миллисекундах).

Подробное описание архитектуры и домена — в [`PROJECT_SUMMARY.md`](PROJECT_SUMMARY.md).
