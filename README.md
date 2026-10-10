# URL Shortener Service

REST-сервис для создания коротких ссылок, перенаправления на исходные URL и просмотра статистики переходов.

## Возможности

- создание короткой ссылки;
- получение информации о ссылке;
- обновление и удаление ссылки;
- редирект по короткому коду;
- счётчик переходов;
- срок жизни ссылки;
- PostgreSQL для хранения данных;
- Redis для кэширования чтения;
- Swagger UI;
- unit-, repository- и controller-тесты;
- запуск всего проекта через Docker Compose.

## Стек

- Java 21;
- Spring Boot 4.1.1;
- Spring MVC, Spring Data JPA, Spring Data Redis;
- PostgreSQL 18.6;
- Redis 8.10.2;
- Flyway;
- Maven;
- JUnit 5, Mockito;
- Docker Compose.

## Структура

```text
src/main/java/ru/ilyalagutin/urlshortener
├── cache       # Redis cache-aside
├── config      # Настройки OpenAPI
├── controller  # HTTP API и редирект
├── dto         # JSON-запросы и ответы
├── entity      # JPA-сущность
├── exception   # Ошибки и единый обработчик
├── repository  # Доступ к PostgreSQL
└── service     # Бизнес-логика
```

## Запуск через Docker

Скопируй пример переменных окружения при необходимости:

```bash
cp .env.example .env
docker compose up --build
```

После запуска:

- API: `http://localhost:8080`;
- Swagger UI: `http://localhost:8080/swagger-ui.html`;
- health check: `http://localhost:8080/actuator/health`.

Остановить сервисы:

```bash
docker compose down
```

## API

Создать ссылку:

```http
POST /api/v1/shorten
Content-Type: application/json

{
  "url": "https://example.com/some/long/url",
  "expiresAt": "2026-12-31T23:59:59Z"
}
```

Получить данные, обновить или удалить ссылку:

```text
GET    /api/v1/shorten/{shortCode}
PUT    /api/v1/shorten/{shortCode}
DELETE /api/v1/shorten/{shortCode}
GET    /api/v1/shorten/{shortCode}/stats
```

Перейти по короткой ссылке:

```text
GET /{shortCode}
```

Ответом будет `302 Found` с заголовком `Location`. При таком переходе счётчик увеличивается на единицу.

## Локальный запуск без Docker

Для запуска напрямую нужны PostgreSQL и Redis, доступные на стандартных портах:

```bash
mvn spring-boot:run
```

Для проверки тестов используется H2:

```bash
mvn test
```

## CI/CD

GitHub Actions запускается при каждом `push` и Pull Request.

CI выполняет следующие проверки:

- запускает Maven-тесты;
- проверяет конфигурацию Docker Compose;
- собирает Docker-образ приложения.

Результаты workflow доступны во вкладке `Actions` репозитория на GitHub.

Публикацию Docker-образа и развёртывание на сервере можно добавить следующим этапом после выбора registry и сервера.
