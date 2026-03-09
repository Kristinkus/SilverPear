# SilverPear 

## Интернет-магазин парфюмерии и косметики
Веб-приложение для управления списком товаров (парфюмерия и косметика) в интернет-магазине.

## Описание проекта
Данный проект представляет собой RESTful веб-приложение, разработанное с использованием Spring Boot. Оно позволяет выполнять базовые CRUD-операции (создание, чтение, обновление, удаление) для сущности "Товар" (`Product`), а также предоставляет расширенный поиск по различным критериям.

В качестве хранилища данных используется **In-Memory репозиторий**, что позволяет запускать приложение без настройки внешней базы данных. Приложение построено с соблюдением многослойной архитектуры (контроллер, сервис, репозиторий) и использует DTO (`UserDto`) для передачи данных между слоями, скрывая внутреннюю структуру сущности и добавляя вычисляемое поле (`salePrice`).

## Аналоги
Основным аналогом является приложение "Gold Apple".

## Модель данных

### Сущности:
- **Product** (родительский класс для всех товаров)
- **Perfume** (парфюмерия с нотами: верхние, средние, базовые)
- **Cosmetics** (косметика с типом кожи, назначением, финишем)
- **User** (пользователи с логином, паролем, контактными данными)
- **Order** (заказы с номером, датой, статусом)
- **OrderItem** (позиции заказа с количеством и ценой)
- **Favorites** (Избранные товары)

### Связи:
- **OneToMany**: Один пользователь → много заказов
- **ManyToMany**: Товары и заказы (через связующую таблицу OrderItem)

## Выполняемые функции

### Управление товарами (CRUD)

**1. Получение списка всех товаров**
- **Метод:** `GET`
- **Эндпоинт:** `/api/products`
- **Описание:** Возвращает список всех товаров в формате DTO.

**2. Получение товара по ID**
- **Метод:** `GET`
- **Эндпоинт:** `/api/products/{id}`
- **Описание:** Возвращает детальную информацию о товаре.

**3. Добавление нового товара**
- **Метод:** `POST`
- **Эндпоинт:** `/api/products`
- **Описание:** Сохраняет новый товар и возвращает созданную запись.

**4. Обновление товара**
- **Метод:** `PUT` (полное обновление)
- **Эндпоинт:** `/api/products/{id}`
- **Метод:** `PATCH` (частичное обновление)
- **Эндпоинт:** `/api/products/{id}`

**5. Удаление товара**
- **Метод:** `DELETE`
- **Эндпоинт:** `/api/products/{id}`

**6. Поиск товаров**
- **Метод:** `GET`
- **Эндпоинт:** `/api/products/search`
- **Параметры:** `name`, `brand`, `category`

### Управление парфюмерией

**1. Получение всех парфюмов**
- **Метод:** `GET`
- **Эндпоинт:** `/api/perfumes`

**2. Создание парфюма**
- **Метод:** `POST`
- **Эндпоинт:** `/api/perfumes`
- **Особенности:** Поля `topNotes`, `middleNotes`, `baseNotes`

### Управление косметикой

**1. Получение всей косметики**
- **Метод:** `GET`
- **Эндпоинт:** `/api/cosmetics`

**2. Создание косметики**
- **Метод:** `POST`
- **Эндпоинт:** `/api/cosmetics`
- **Особенности:** Поля `prescription`, `skinType`, `finish`

### Управление пользователями

**1. CRUD операции для пользователей (Users)**
- `GET /api/users` - все пользователи
- `GET /api/users/{id}` - пользователь по ID
- `POST /api/users` - создание
- `PUT /api/users/{id}` - обновление
- `DELETE /api/users/{id}` - удаление

**2. CRUD операции для товаров (Products)**
- `GET /api/products` - все товары
- `GET /api/products/{id}` - товар по ID
- `POST /api/products` - создание
- `PUT /api/products/{id}` - полное обновление
- `PATCH /api/products/{id}` - частичное обновление
- `DELETE /api/products/{id}` - удаление

**3. CRUD операции для парфюмерии (Perfume)**
- `GET /api/perfumes` - все духи
- `GET /api/perfumes/{id}` - духи по ID
- `POST /api/perfumes` - создание
- `PUT /api/perfumes/{id}` - обновление
- `DELETE /api/perfumes/{id}` - удаление

**4. CRUD операции для косметики (Cosmetics)**
- `GET /api/cosmetics` - вся косметика
- `GET /api/cosmetics/{id}` - косметика по ID
- `POST /api/cosmetics` - создание
- `PUT /api/cosmetics/{id}` - обновление
- `DELETE /api/cosmetics/{id}` - удаление

**5. CRUD операции для заказов (Orders)**
- `GET /api/orders/demo/without-nplus1` - все заказы с оптимизацией
- `GET /api/orders/demo/nplus1` - все заказы с проблемой N+1
- `GET /api/orders/demo/{orderId}` - заказ по ID
- `POST /api/orders/demo/with-transaction?userId={id}` - создание с транзакцией
- `POST /api/orders/demo/without-transaction?userId={id}` - создание без транзакции
- `PUT /api/orders/demo/{orderId}` - обновление заказа
- `PATCH /api/orders/demo/{userId}/orders/{orderId}?status={status}` - обновление статуса
- `DELETE /api/orders/demo/{orderId}` - удаление

**6. CRUD операции для избранного (Favorites)**
- `GET /api/users/{userId}/favorites` - все избранное пользователя
- `POST /api/users/{userId}/favorites/{productId}` - добавить в избранное
- `DELETE /api/users/{userId}/favorites/{productId}` - удалить из избранного

**7. Поиск и фильтрация**
- `GET /api/products/search?name={name}` - поиск по названию
- `GET /api/products/search?brand={brand}` - поиск по бренду
- `GET /api/products/search?category={category}` - поиск по категории
- `GET /api/products/search?name={name}&brand={brand}&category={category}` - комбинированный поиск

### Управление заказами

**1. Демонстрация проблемы N+1**
- **Метод:** `GET`
- **Эндпоинт:** `/api/orders/demonstrate-nplus1/{userId}`

**2. Демонстрация решения с @EntityGraph**
- **Метод:** `GET`
- **Эндпоинт:** `/api/orders/demonstrate-solution/{userId}`

**3. Создание заказа с транзакцией (полный откат при ошибке)**
- **Метод:** `POST`
- **Эндпоинт:** `/api/orders/create-with-items?userId={id}`
- **Тело:** `{"productIds": [1,2], "quantities": [2,1]}`

**4. Создание заказа без транзакции (частичное сохранение)**
- **Метод:** `POST`
- **Эндпоинт:** `/api/orders/create-without-transaction?userId={id}`
- **Тело:** `{"productIds": [1,2,3], "quantities": [2,1,3]}`

## Ключевые концепции (согласно ТЗ)

### 1. Проблема N+1 и её решение
**Где проявляется:** `OrderService.demonstrateNPlusOneProblem()`
- 1 запрос на получение заказов + N запросов на товары в каждом заказе

**Где решается:** `OrderRepository.findByUserWithItemsAndProducts()`
- Использование `@EntityGraph(attributePaths = {"orderItems", "orderItems.product"})`
- Один запрос с JOIN загружает все связанные данные

### 2. Транзакции
**Частичное сохранение (без @Transactional):**
- `OrderService.createOrderWithoutTransaction()`
- При ошибке заказ сохраняется, но товары не добавляются

**Полный откат (с @Transactional):**
- `OrderService.createOrderWithItems()`
- При ошибке все изменения отменяются

## Стек технологий
- Язык: Java 21

- Фреймворк: Spring Boot 4.0.2

- База данных: MySQL

- Сборщик: Maven

- ORM: Hibernate (JPA)


## Ссылка на проверку SonarCloud

https://sonarcloud.io/project/overview?id=Kristinkus_SilverPear