# Инструкция по запуску проекта

## Требования

Для запуска проекта необходимо иметь установленные:
- Docker
- Docker Compose
- Java 21 JDK
- Maven (или можно использовать включенный в проект Maven Wrapper)

## Шаги по запуску

1. Клонируйте репозиторий:
```bash
git clone [URL репозитория]
cd oldLab_client
```

2. Настройка конфигурации:
   - Убедитесь, что файл `src/main/resources/application.yaml` настроен правильно
   - При необходимости измените параметры в `docker-compose.yaml`

3. Запуск инфраструктуры через Docker Compose:
```bash
docker-compose up -d
```

Это запустит следующие сервисы:
- PostgreSQL (порт 5432)
- Redis (порт 6379)
- Kafka (порт 9092)
- Elasticsearch (порт 9200)
- Kibana (порт 5601)
- MinIO (порты 9000 для API и 9001 для веб-консоли)
- PgAdmin (порт 7313)

4. Запуск приложения:

Через Maven Wrapper:
```bash
./mvnw spring-boot:run
```

или через Docker Compose (автоматически):
Приложение запустится автоматически как сервис `authentication-service` на порту 8080

## Доступ к сервисам

- Основное приложение: http://localhost:8080
- PgAdmin: http://localhost:7313
  - Email: ax@micro.kz
  - Password: root
- Kibana: http://localhost:5601
- MinIO Console: http://localhost:9001
  - Access Key: 123456789qwerty123456789qwerty123456789qwerty
  - Secret Key: 123456789qwerty123456789qwerty123456789qwerty

## База данных

- Host: localhost (или authentication-db внутри Docker network)
- Port: 5432
- Database: authenticationdb
- Username: postgres
- Password: 1

## Остановка проекта

Для остановки всех сервисов выполните:
```bash
docker-compose down
```

Для удаления всех данных (включая тома):
```bash
docker-compose down -v
```
