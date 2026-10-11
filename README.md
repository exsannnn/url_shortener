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

Подробная схема компонентов и потоков запросов находится в [README_ARCHITECTURE.md](README_ARCHITECTURE.md).

## Запуск через Docker

Запусти приложение и его зависимости:

```bash
docker compose up --build
```

По умолчанию используются локальные значения для PostgreSQL и Redis. При необходимости можно создать свой `.env` рядом с `compose.yaml`; этот файл не нужно добавлять в Git.

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
mvn verify
```

Для запуска только тестов:

```bash
mvn test
```

## Быстрая демонстрация

После запуска приложения можно выполнить:

```bash
curl -i -X POST http://localhost:8080/api/v1/shorten \
  -H "Content-Type: application/json" \
  -d '{"url":"https://example.com"}'
```

Из ответа нужно взять `shortCode`, а затем проверить редирект:

```bash
curl -i http://localhost:8080/{shortCode}
```

Ожидаемый результат — `302 Found` и заголовок `Location: https://example.com`.

## Что можно рассказать на собеседовании

- PostgreSQL используется для постоянного хранения ссылок и статистики.
- Redis уменьшает количество чтений из базы при повторных переходах.
- Flyway создаёт таблицы и индексы при запуске приложения.
- Счётчик переходов увеличивается отдельным SQL-запросом.
- Тесты разделены на unit-, controller- и repository-тесты.

## CI/CD

GitHub Actions запускается при каждом `push` и Pull Request.

CI выполняет следующие проверки:

- запускает Maven-проверки через `verify`;
- проверяет конфигурацию Docker Compose;
- собирает Docker-образ приложения;
Workflow запускается автоматически для push и Pull Request. Он проверяет тесты, конфигурацию Compose и сборку Docker-образа.