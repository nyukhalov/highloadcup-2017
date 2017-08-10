
# API

### GET /<entity>/<id> для получения данных о сущности
when data should be refreshed:
- entity was updated

### GET /users/<id>/visits для получения списка посещений пользователем
when data should be refreshed:
- visit was updated
  - just updated
  - changed user
- visit was added

### GET /locations/<id>/avg для получения средней оценки достопримечательности
when data should be updated:
- visit was updated
  - location id or mark
- visit was added

### POST /<entity>/<id> на обновление

### POST /<entity>/new на создание


