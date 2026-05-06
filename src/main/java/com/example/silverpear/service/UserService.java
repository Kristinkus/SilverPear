package com.example.silverpear.service;

import com.example.silverpear.enums.UserRole;
import com.example.silverpear.product.entity.User;
import com.example.silverpear.product.mapper.UserMapper;
import com.example.silverpear.product.mapper.UserWithOrdersMapper;
import com.example.silverpear.product.productdto.RegisterRequest;
import com.example.silverpear.product.productdto.AdminUserListDto;
import com.example.silverpear.product.productdto.UserProfilePatchRequest;
import com.example.silverpear.product.productdto.UserRequest;
import com.example.silverpear.product.productdto.UserResponse;
import com.example.silverpear.product.productdto.UserWithOrdersDto;
import com.example.silverpear.repository.UserRepository;
import com.example.silverpear.util.PhoneLoginNormalizer;
import org.springframework.http.HttpStatus;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserWithOrdersMapper userWithOrdersMapper;
    private final PasswordEncoder passwordEncoder;
    private final OrderService orderService;

    public UserService(UserRepository userRepository,
                       UserMapper userMapper,
                       UserWithOrdersMapper userWithOrdersMapper,
                       PasswordEncoder passwordEncoder,
                       @Lazy OrderService orderService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.userWithOrdersMapper = userWithOrdersMapper;
        this.passwordEncoder = passwordEncoder;
        this.orderService = orderService;
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id: " + id));
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserWithOrdersDto getUserWithOrders(Long id) {
        User user = userRepository.findByIdWithOrdersAndItems(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id: " + id));
        List<Long> brokenOrderIds = new ArrayList<>();
        user.getOrders().forEach(order -> {
            if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
                brokenOrderIds.add(order.getId());
            }
        });
        if (!brokenOrderIds.isEmpty()) {
            brokenOrderIds.forEach(orderService::deleteOrder);
            user = userRepository.findByIdWithOrdersAndItems(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id: " + id));
        }
        return userWithOrdersMapper.toDto(user);
    }

    public UserResponse register(RegisterRequest registerRequest) {
        if (registerRequest == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Пустое тело запроса");
        }
        String phoneRaw = resolveRegisterPhoneRaw(registerRequest);
        String password = registerRequest.getPassword() != null ? registerRequest.getPassword() : "";
        if (password.length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Пароль: не менее 8 символов");
        }
        String login = PhoneLoginNormalizer.toLogin(phoneRaw);
        if (!isPlausibleE164Login(login)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Введите полный номер телефона с кодом страны");
        }
        String structuredSurname = trimToNull(registerRequest.getSurname());
        String structuredName = trimToNull(registerRequest.getName());
        String structuredPatronymic = trimToNull(registerRequest.getPatronymic());
        if (structuredSurname != null && structuredName != null && structuredPatronymic != null) {
            UserRequest structured = buildUserRequestFromStructured(login, password,
                    structuredSurname, structuredName, structuredPatronymic);
            return createUserWithRole(structured, UserRole.USER);
        }
        String fioLine = resolveRegisterFio(registerRequest);
        if (!StringUtils.hasText(fioLine)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Укажите фамилию, имя и отчество в отдельных полях или ФИО одной строкой");
        }
        fioLine = fioLine.trim().replaceAll("\\s+", " ");
        if (fioLine.length() < 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Укажите ФИО полностью");
        }
        UserRequest request = buildUserRequestFromFio(login, password, fioLine);
        return createUserWithRole(request, UserRole.USER);
    }

    public UserResponse patchProfile(Long id, UserProfilePatchRequest body) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id: " + id));
        user.setSurname(clampPersonPart(body.getSurname()));
        user.setName(clampPersonPart(body.getName()));
        user.setPatronymic(clampPersonPart(body.getPatronymic()));
        User saved = userRepository.save(user);
        return userMapper.toResponse(saved);
    }

    private static String clampPersonPart(String raw) {
        String t = raw.trim().replace('\u00A0', ' ');
        if (t.length() > 30) {
            return t.substring(0, 30);
        }
        return t;
    }

    private UserRequest buildUserRequestFromStructured(String login, String password,
                                                       String surname, String name, String patronymic) {
        UserRequest request = new UserRequest();
        request.setLogin(login);
        request.setPassword(password);
        request.setSurname(clampPersonPart(surname));
        request.setName(clampPersonPart(name));
        request.setPatronymic(clampPersonPart(patronymic));
        request.setEmail(null);
        request.setPhone(login);
        return request;
    }

    private static String resolveRegisterPhoneRaw(RegisterRequest rr) {
        String fromPhone = trimToNull(rr.getPhone());
        if (fromPhone != null) {
            return normalizeSpaces(fromPhone);
        }
        String fromLogin = trimToNull(rr.getLogin());
        if (fromLogin != null && looksLikePhoneInput(fromLogin)) {
            return normalizeSpaces(fromLogin);
        }
        return "";
    }

    private static String resolveRegisterFio(RegisterRequest rr) {
        String fi = trimToNull(rr.getFio());
        if (fi != null) {
            return fi;
        }
        String n = trimToNull(rr.getName());
        String s = trimToNull(rr.getSurname());
        if (n != null && s != null) {
            return n + " " + s;
        }
        if (n != null) {
            return n;
        }
        return s;
    }

    private static String trimToNull(String s) {
        if (!StringUtils.hasText(s)) {
            return null;
        }
        String t = normalizeSpaces(s.trim());
        return t.isEmpty() ? null : t;
    }

    private static String normalizeSpaces(String s) {
        return s.replace('\u00A0', ' ');
    }

    private static boolean looksLikePhoneInput(String s) {
        String t = s.trim();
        return t.startsWith("+") || Character.isDigit(t.charAt(0));
    }

    private static boolean isPlausibleE164Login(String plusDigits) {
        if (!StringUtils.hasText(plusDigits) || !plusDigits.startsWith("+")) {
            return false;
        }
        int digitCount = plusDigits.length() - 1;
        return digitCount >= 10 && digitCount <= 15;
    }

    private UserRequest buildUserRequestFromFio(String login, String password, String fio) {
        int sp = fio.indexOf(' ');
        String name;
        String surname;
        if (sp < 0) {
            name = fio;
            surname = "—";
        } else {
            name = fio.substring(0, sp);
            surname = fio.substring(sp + 1).trim();
            if (!StringUtils.hasText(surname)) {
                surname = "—";
            }
        }
        if (name.length() > 30) {
            name = name.substring(0, 30);
        }
        if (surname.length() > 30) {
            surname = surname.substring(0, 30);
        }
        UserRequest request = new UserRequest();
        request.setLogin(login);
        request.setPassword(password);
        request.setName(name);
        request.setSurname(surname);
        request.setEmail(null);
        request.setPhone(login);
        return request;
    }

    public UserResponse createUser(UserRequest request) {
        return createUserWithRole(request, UserRole.USER);
    }

    private UserResponse createUserWithRole(UserRequest request, UserRole role) {
        if (userRepository.existsByLogin(request.getLogin())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Пользователь с логином «" + request.getLogin() + "» уже существует");
        }
        if (StringUtils.hasText(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Пользователь с email «" + request.getEmail() + "» уже зарегистрирован");
        }
        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        User saved = userRepository.save(user);
        return userMapper.toResponse(saved);
    }

    public UserResponse updateUser(Long id, UserRequest request) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id: " + id));
        if (userRepository.existsByLoginAndIdNot(request.getLogin(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Логин «" + request.getLogin() + "» уже занят другим пользователем");
        }
        if (StringUtils.hasText(request.getEmail()) && userRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Email «" + request.getEmail() + "» уже используется другим пользователем");
        }
        User user = userMapper.toEntity(request);
        user.setId(id);
        user.setRole(existing.getRole());
        user.setGiftBalance(existing.getGiftBalance() != null ? existing.getGiftBalance() : BigDecimal.ZERO);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        if (!StringUtils.hasText(user.getPatronymic())) {
            user.setPatronymic(existing.getPatronymic());
        }
        User updated = userRepository.save(user);
        return userMapper.toResponse(updated);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<AdminUserListDto> getAllUsersForAdmin() {
        return userRepository.findAllUsersWithOrders()
                .stream()
                .filter(user -> user.getRole() != UserRole.ADMIN)
                .map(userMapper::toAdminListDto)
                .toList();
    }
}