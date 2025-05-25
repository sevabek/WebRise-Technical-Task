# WebRise-Technical-Task

Написанное решение полностью покрывает функционал, требуемый заданием, а также добавляет некоторые эндпоинты для более логичной работы с данными.
Весь проект покрыт тестами, как интеграционными, так и юнит. Также было добавлено логирование и миграции через liquibase.

**Запуск:**
```bash
docker-compose up
```

## Эндпоинты

### Users

POST   /users - Создает нового пользователя.
  {
      "username": "john_doe14",
      "email": "john.doe77@example.com",
      "fullName": "John Doe",
      "createdAt": "2024-02-20T10:00:00",
      "subscriptions": [
        {
            "startDate": "2024-02-20T10:00:00",
            "endDate": "2024-02-20T10:00:00",
            "active": true,
            "subscriptionProvider": {
                "id": 3
            }
        }
      ]
  }
GET    /users/{userId} - Получает информацию о пользователе по его идентификатору.
PATCH  /users/{userId} - Обновляет данные существующего пользователя (частично, без createdAt).
  {
      "username": "john_doe3",
      "email": "john.doe3@3example.com",
      "fullName": "John Doe"
  }
DELETE /users/{userId} - Удаляет пользователя по идентификатору.

### Subsccriptions

POST /users/{userId}/subscriptions - Добавляет новую подписку для указанного пользователя.
  {
      "startDate": "2024-02-20T10:00:00",
      "endDate": "2024-05-20T10:00:00",
      "active": true,
      "subscriptionProvider": {
          "id": 3
      }
  }
PUT /users/{userId}/subscriptions/{subId} - Обновляет данные о подписке пользователя.
  {
      "startDate": "2024-02-20T10:00:00",
      "endDate": "2024-06-20T10:00:00",
      "active": false,
      "subscriptionProvider": {
          "id": 2
      }
  }
GET /users/{userId}/subscriptions/ - Получает список всех подписок пользователя.
DELETE /users/{userId}/subscriptions/{subId} - Удаляет конкретную подписку пользователя.

### SubscriptionProviders

GET /subscription-provider/{id} - Получает информацию о провайдере подписки по идентификатору.
POST /subscription-provider - Создает нового провайдера подписок.
  {
      "name": "New Streaming Service",
      "price": 12.99
  }
PUT /subscription-provider/{id} - Обновляет данные существующего провайдера подписок.
  {
      "name": "Updated Streaming Service",
      "price": 14.99
  }
DELETE /subscription-provider/{id} - Удаляет провайдера подписок по идентификатору.

### TopSubscriptions

GET /subscriptions/top - Возвращает список самых популярных подписок (по умолчанию 3).
