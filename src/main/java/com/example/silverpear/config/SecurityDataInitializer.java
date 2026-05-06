package com.example.silverpear.config;

import com.example.silverpear.enums.Gender;
import com.example.silverpear.enums.UserRole;
import com.example.silverpear.product.entity.Product;
import com.example.silverpear.product.entity.User;
import com.example.silverpear.repository.FavoriteRepository;
import com.example.silverpear.repository.ProductRepository;
import com.example.silverpear.repository.UserRepository;
import com.example.silverpear.service.CacheService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class SecurityDataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final FavoriteRepository favoriteRepository;
    private final ProductRepository productRepository;
    private final CacheService cacheService;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Если true — при каждом старте приложения БД очищается и заново заливаются демо-пользователи и товары.
     * Для обычной работы (сохранение регистраций) держите false.
     */
    @Value("${app.data.reset-on-startup:false}")
    private boolean resetOnStartup;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (resetOnStartup) {
            wipeAllBusinessData();
            entityManager.clear();
            seedDemoUsers();
            widenProductGenderColumnForEnumStrings();
            widenProductDescriptionColumn();
            insertDemoProductRows(demoProducts(), false);
            cacheService.clearAll();
            return;
        }

        try {
            widenProductGenderColumnForEnumStrings();
        } catch (Exception ignored) {
            // колонка уже подходящая или нет прав на ALTER
        }
        try {
            widenProductDescriptionColumn();
        } catch (Exception ignored) {
            // уже LONGTEXT или нет прав на ALTER
        }

        if (userRepository.count() == 0) {
            seedDemoUsers();
        }
        cleanupDanglingFavoriteLinks();
        if (productRepository.count() == 0) {
            insertDemoProductRows(demoProducts(), false);
        }
        cacheService.evictByPattern("Product:findAll");
        cacheService.evictByPattern("Product:findByCategory");
    }

    private void seedDemoUsers() {
        userRepository.saveAll(List.of(
                buildUser("admin", "admin", "Админ", "SilverPear", "admin@silverpear.local", "+375 29 000 00 00", UserRole.ADMIN),
                buildUser("testuser", "testuser123", "Тест", "Пользователь", "testuser@silverpear.local", "+375 29 111 11 11", UserRole.USER),
                buildUser("kristina", "kristina123", "Кристина", "Усевич", "kristy2202lovely@gmail.com", "+375 29 676 77 36", UserRole.USER)
        ));
    }

    private List<Product> demoProducts() {
        return List.of(
                buildProduct(31L, "GO TAPA Bakuchiol firming cream", "GO TAPA", goTapaBakuchiolFirmingCreamDescription(),
                        "Уход", 21.83, true, 4, "FACE_CREAM", Gender.UNISEX, 10, "/products/go-tapa-bakuchiol-firming-cream.png"),
                buildProduct(30L, "VIVIENNE SABO Fixateur Lamination", "VIVIENNE SABO", vivienneSaboFixateurLaminationDescription(),
                        "Макияж", 18.50, true, 58, "BROW_GEL", Gender.UNISEX, 6, "/products/vivienne-sabo-fixateur-lamination.png"),
                buildProduct(29L, "ICE Repair My Hair Mask", "ICE PROFESSIONAL", iceRepairMyHairMaskDescription(),
                        "Уход", 19.60, true, 26, "HAIR_MASK", Gender.UNISEX, 250, "/products/ice-repair-my-hair-mask.png"),
                buildProduct(28L, "Zeitun Wedding Day Body Scrub", "ZEITUN", zeitunWeddingDayBodyScrubDescription(),
                        "Уход", 24.90, true, 31, "BODY_SCRUB", Gender.UNISEX, 250, "/products/zeitun-wedding-day-body-scrub.png"),
                buildProduct(27L, "MYTAUI meraki coral sand scrub", "MYTAUI", mytauiMerakiCoralSandScrubDescription(),
                        "Уход", 40.70, true, 18, "BODY_SCRUB", Gender.UNISEX, 300, "/products/mytaui-meraki-coral-sand-scrub.png")
        );
    }

    private void keepOnlyRealProducts() {
        final String keepName1 = "VIVIENNE SABO Fixateur Lamination";
        final String keepName2 = "GO TAPA Bakuchiol firming cream";
        final String selector = "LOWER(name) NOT IN (LOWER(?), LOWER(?))";
        jdbcTemplate.update("DELETE FROM user_favorites WHERE product_id IN (SELECT id FROM products WHERE " + selector + ")", keepName1, keepName2);
        jdbcTemplate.update("DELETE FROM order_items WHERE product_id IN (SELECT id FROM products WHERE " + selector + ")", keepName1, keepName2);
        jdbcTemplate.update("DELETE FROM cosmetics WHERE id IN (SELECT id FROM products WHERE " + selector + ")", keepName1, keepName2);
        jdbcTemplate.update("DELETE FROM perfume WHERE id IN (SELECT id FROM products WHERE " + selector + ")", keepName1, keepName2);
        jdbcTemplate.update("DELETE FROM products WHERE " + selector, keepName1, keepName2);
    }

    private static String goTapaBakuchiolFirmingCreamDescription() {
        return """
                Для чего: Когда гравитация начинает работать против вас.

                Легкая, как перышко, текстура крема основательно укрепляет кожу, увлажняет и противостоит потере влаги.
                Мощный бакучиол, суперзвезда современного антивозрастного ухода, работает на уровне дермы и усиливает синтез коллагена и замедляет его распад.
                Идеальный вариант, если требуется эффект ретинола, но без раздражения. Крем достаточно легкий, чтобы наносить и на зону вокруг глаз.

                Для лучшего проникновения крема и более видимого эффекта, перед нанесением используйте сыворотку.
                Подходит всем типам кожи.

                А что еще?
                • усиливает синтез коллагена
                • предотвращает потерю упругости
                • укрепляет кожный каркас
                """;
    }

    private static String vivienneSaboFixateurLaminationDescription() {
        return """
                артикул: 19000215138

                FIXATEUR LAMINATION EFFECT — гель для бровей от VIVIENNE SABÓ для экстрасильной фиксации с эффектом ламинирования на 16 часов. Благодаря его суперстойкой формуле и удобной щеточке получится уложить даже непослушные и жесткие брови.
                Гель с ультралегкой текстурой мгновенно придает форму бровям и моментально фиксирует их без склеивания и белого налета. Спиралевидная щеточка среднего размера с ворсинками набирает нужное количество массы, чтобы придать желаемую форму даже жестким бровям, добавить им объем и текстурность.
                С FIXATEUR LAMINATION EFFECT легко создать естественную укладку и фиксацию с эффектом ламинирования без похода в салон.
                """;
    }

    private static String iceRepairMyHairMaskDescription() {
        return """
                Восстанавливающая маска для волос с мягкой кремовой текстурой.
                Помогает уменьшить ломкость, разглаживает длину и придает волосам плотность и блеск.
                """;
    }

    private static String zeitunWeddingDayBodyScrubDescription() {
        return """
                Скраб для тела с деликатными отшелушивающими частицами и ароматом розы.
                Мягко обновляет кожу, делает ее гладкой и более ровной на ощупь.
                """;
    }

    private static String mytauiMerakiCoralSandScrubDescription() {
        return """
                Скраб для тела с текстурой кораллового песка для интенсивного, но бережного ухода.
                Подходит для регулярного применения, помогает поддерживать гладкость и тонус кожи.
                """;
    }

    private static String darlingGlistenDescription() {
        return """
                артикул: 19000007953

                Я твой легкий ХАЙЛАЙТЕР-СТИК, который всегда под рукой для того, чтобы создать СВЕЖИЙ образ с эффектом ВЛАЖНОГО СИЯНИЯ.

                Зачем мне это?
                Шелковая текстура этого хайлайтера легко поддается растушевке и ложится как вторая кожа, создавая потрясающее естественное сияние. GLISTEN подстраивается под оттенок кожи и обеспечивает эффект натурального стробинга, придавая образу свежесть.

                А еще он насыщен увлажняющим фитоскваланом и ухаживает за кожей.
                """;
    }

    private static String radShineOnThruHighlighterDescription() {
        return """
                артикул: 19000005764

                Если описать хайлайтер одним словом — вау!
                А если конкретнее, то это нежная пудровая текстура, которая способна на очень насыщенное красивое сияние, поэтому для нанесения достаточно пары лёгких движений.
                На финише можно добиться любой степени блеска — от лёгкого glow до того, который будет видно из космоса.
                """;
    }

    private static String radGoBrightAlloverDescription() {
        return """
                артикул: 19000003031

                Парочка движений и естественное красивое сияние уже радует глаз!
                Формат стика невероятно удобен и эффективен, а если нежный сияющий оттенок вам по душе, то Go Bright Allover может претендовать на роль фаворита в косметичке.
                """;
    }

    private static String planetaOrganicaPureShampooDescription() {
        return """
                артикул: 19000141659

                Мягкий шампунь для волос из коллекции PURE разработан для чувствительной и склонной к аллергии кожи головы. Идеальное решение на каждый день для всех типов волос. Деликатно очищает, делает волосы блестящими, гладкими и мягкими.

                Инновационный комплекс ExtPine® основан на натуральной водной вытяжке из «золотой» сосны*. Клинически доказано, ExtPine® нормализует микробиом кожи, улучшает тонус, сокращает расширенные поры и их количество. Защищает гидролипидный слой и проявляет себя как бустер для защиты кожи от внешних факторов.
                * Подвид сосны, произрастающей в сибирском регионе. Обладает корой золотистого цвета, за что и названа «золотой».

                АКТИВНЫЕ ГИПОАЛЛЕРГЕННЫЕ КОМПОНЕНТЫ [ India blend ]
                ИНДИЙСКИЙ МЫЛЬНЫЙ ОРЕХ – натуральная пенящаяся основа. Полностью гипоаллергенна.
                ОГУРЕЧНАЯ ТРАВА – природный источник фолиевой кислоты, которая ускоряет заживление поврежденной кожи. Оказывает противовоспалительный эффект, успокаивает раздраженную кожу при зуде.
                ГИПОАЛЛЕРГЕННОСТЬ ПОДТВЕРЖДЕНА ИНСТИТУТОМ СИБИРСКОЙ КОСМЕТИКИ

                НЕ СОДЕРЖИТ: отдушек, красителей, эфирных масел, аллергенов, спирта, агрессивных ПАВ, парабенов. Не тестируется на животных.
                """;
    }

    private static String essencePrincessFalseLashDescription() {
        return """
                артикул: 19000457905

                В городе появилась новая сказочная принцесса! Бордовая тушь для ресниц с эффектом накладных ресниц Lash PRINCESS от essence — новинка в семействе тушей для ресниц Lash Princess.
                Особая конусообразная форма фибровой щеточки придает ресницам длину и выразительный объем, а также создает эффект накладных ресниц.
                В ее формулу также входят ухаживающие растительные воски.
                А упаковка, как и у ее сестер, украшена великолепным вечерним платьем — на этот раз в потрясающем сочетании бордового и нежно-нюдового оттенков.
                """;
    }

    private static String kikoBrightLiftDayDescription() {
        return """
                артикул: 19000134996

                Осветляющий дневной лифтинг крем с морским коллагеном. Он тонизирует кожу и уменьшает морщины*, восстанавливая сияние молодости на лице. Улучшает свойства и стойкость макияжа*. Активные ингредиенты защищают кожу от оксидативного стресса и придают ей здоровое сияние.

                Формула содержит морской коллаген, витамин С и революционный комплекс ActiGlow, который возвращает красоту коже и вместе с тем улучшает макияж.

                Кремовая, шелковистая текстура ложится на кожу, оставляя на ней мягкий аромат камелии и розы.

                Крем гладко ложится на кожу и придаёт ей увлажнение*, упругость* и эластичность*. Он также содержит солнцезащитный экран, который защищает кожу.

                Идеально подходит для нормальной и сухой кожи.

                Продукт прошёл дерматологические испытания.

                Средство не вызывает угревую сыпь.

                *Результат клинически-инструментального испытания с участием 20 женщин, которые использовали крем Bright Lift Day в течение 28 дней.
                """;
    }

    private static String neydoMosslandDescription() {
        return """
                артикул: 19000200197

                Сон Роберто о затерявшемся в заколдованных лесах страннике. Его окружают огромные кедры и кипарисы, воздух хрустально чист, а солнечные лучи, пронизывающие густую чащу, указывают путь. Аромат-приключение, в котором запах можжевельника и ветивера оттеняют цветочные ноты гвоздики и пряных специй.

                Roberto, 12.09
                В ушах свистит ветер, кожу обжигает холод. Да ладно! Я лечу?! И… абсолютно голый? От неожиданности и смущения перехватывает дыхание, но мысль работает четко. Нужно собраться, сконцентрироваться, приземлиться. Стремительно несусь через облака: одно, второе, третье, – и вдруг все замедляется. Бешеный стук сердца успокаивается, я почти теряю сознание. Окончательно провалиться в темноту не дает шорох листвы. Очнувшись, понимаю, что уже планирую над тропическим лесом.

                Уфф! Опускаюсь на землю. Осматриваюсь. Надо мной высоченные белые кедры и кипарисы. Перевожу дыхание. Как же хорошо стоять ногами на земле! Но мне холодно, нужно найти хоть что-то, чем можно прикрыться. Иду. Лес. Запахи. Вдруг – поляна, покрытая серебристым мхом, мягким и теплым. Срываю покров и заворачиваюсь в него, как в мантию. Мне тепло, и я чувствую, что я принц этой чащи! Скручиваю себе подобие короны из можжевельника и ветивера. Пью нектар цветов, взлетаю на любое дерево.
                Я эльф? Не знаю, но теперь это мой дом. Не будите меня!

                Страна происхождения товара может отличаться (Республика Корея/Франция).
                """;
    }

    /**
     * В старых схемах gender мог быть ENUM/CHAR(1) — тогда строки FEMALE, UNISEX дают "Data truncated".
     * Hibernate с {@link com.example.silverpear.enums.Gender} ожидает хранение имён enum как строка.
     */
    /**
     * MySQL/MariaDB only — H2 (tests) and others do not support {@code MODIFY COLUMN}.
     */
    private boolean isMysqlLike() {
        try (Connection c = Objects.requireNonNull(jdbcTemplate.getDataSource()).getConnection()) {
            String name = c.getMetaData().getDatabaseProductName();
            return "MySQL".equalsIgnoreCase(name) || name.toLowerCase().contains("mariadb");
        } catch (SQLException e) {
            return false;
        }
    }

    private void widenProductGenderColumnForEnumStrings() {
        if (!isMysqlLike()) {
            return;
        }
        jdbcTemplate.execute("ALTER TABLE products MODIFY COLUMN gender VARCHAR(32) NULL");
    }

    /**
     * Старые схемы с VARCHAR для описания не вмещают длинные тексты (NEYDO и др.); Hibernate ddl-auto не всегда меняет тип.
     */
    private void widenProductDescriptionColumn() {
        if (!isMysqlLike()) {
            return;
        }
        jdbcTemplate.execute("ALTER TABLE products MODIFY COLUMN description LONGTEXT NULL");
    }

    /**
     * @param onlyIfMissing true — не трогать уже существующие id (догрузка новых товаров в существующую БД).
     */
    private void insertDemoProductRows(List<Product> products, boolean onlyIfMissing) {
        String sql = """
                INSERT INTO products (id, name, brand, description, category, sale_price, in_stock, stock_quantity, `type`, gender, volume, image_url)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        for (Product p : products) {
            if (onlyIfMissing && productRepository.existsById(p.getId())) {
                continue;
            }
            jdbcTemplate.update(sql,
                    p.getId(),
                    p.getName(),
                    p.getBrand(),
                    p.getDescription(),
                    p.getCategory(),
                    p.getSalePrice(),
                    p.isInStock(),
                    p.getStockQuantity(),
                    p.getType(),
                    p.getGender().name(),
                    p.getVolume(),
                    p.getImageUrl());
        }
    }

    private void wipeAllBusinessData() {
        favoriteRepository.clearAllFavoritesLinks();
        favoriteRepository.clearAllFavoriteBrandLinks();
        entityManager.createNativeQuery("DELETE FROM gift_card_orders").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM order_items").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM orders").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM cosmetics").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM perfume").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM products").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM users").executeUpdate();
        entityManager.flush();
        entityManager.clear();
    }

    /**
     * После ручных удалений товаров остаются ссылки в user_favorites в старых инсталляциях без строгих FK.
     * Чистим «висячие» связи, чтобы избранное/аккаунт открывались без ошибок.
     */
    private void cleanupDanglingFavoriteLinks() {
        try {
            jdbcTemplate.update("""
                    DELETE FROM user_favorites
                    WHERE product_id IS NULL
                       OR NOT EXISTS (SELECT 1 FROM products p WHERE p.id = user_favorites.product_id)
                    """);
        } catch (Exception ignored) {
            // H2/CI: таблица может отсутствовать или иметь другое имя до полного DDL — не мешаем старту.
        }
    }

    private User buildUser(String login, String password, String name, String surname,
                           String email, String phone, UserRole role) {
        User user = new User();
        user.setLogin(login);
        user.setPassword(passwordEncoder.encode(password));
        user.setName(name);
        user.setSurname(surname);
        user.setEmail(email);
        user.setPhone(phone);
        user.setRole(role);
        return user;
    }

    private Product buildProduct(Long id, String name, String brand, String description, String category,
                                 double salePrice, boolean inStock, int stockQuantity, String type, Gender gender, double volume) {
        return buildProduct(id, name, brand, description, category, salePrice, inStock, stockQuantity, type, gender, volume, null);
    }

    private Product buildProduct(Long id, String name, String brand, String description, String category,
                                 double salePrice, boolean inStock, int stockQuantity, String type, Gender gender, double volume,
                                 String imageUrl) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setBrand(brand);
        product.setDescription(description);
        product.setCategory(category);
        product.setSalePrice(salePrice);
        product.setInStock(inStock);
        product.setStockQuantity(stockQuantity);
        product.setType(type);
        product.setGender(gender);
        product.setVolume(volume);
        product.setImageUrl(imageUrl);
        return product;
    }
}
