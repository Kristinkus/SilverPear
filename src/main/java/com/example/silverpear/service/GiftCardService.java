package com.example.silverpear.service;

import com.example.silverpear.product.entity.GiftCardOrder;
import com.example.silverpear.product.entity.User;
import com.example.silverpear.product.productdto.GiftCardOrderRequest;
import com.example.silverpear.product.productdto.GiftCardOrderResponse;
import com.example.silverpear.repository.GiftCardOrderRepository;
import com.example.silverpear.repository.UserRepository;
import com.example.silverpear.util.PhoneLoginNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class GiftCardService {

    private final GiftCardOrderRepository giftCardOrderRepository;
    private final UserRepository userRepository;

    @Transactional
    public GiftCardOrderResponse create(Long senderUserId, GiftCardOrderRequest request) {
        GiftCardOrder order = new GiftCardOrder();
        order.setSenderUserId(senderUserId);
        order.setDesignId(request.getDesignId());
        order.setAmount(request.getAmount());
        order.setRecipientPhone(PhoneLoginNormalizer.toLogin(request.getRecipientPhone()));
        GiftCardOrder saved = giftCardOrderRepository.save(order);
        creditRecipientGiftBalance(saved.getRecipientPhone(), saved.getAmount());
        return new GiftCardOrderResponse(
                saved.getId(),
                saved.getDesignId(),
                saved.getAmount(),
                saved.getRecipientPhone(),
                saved.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public List<GiftCardOrderResponse> listReceivedByRecipient(Long recipientUserId) {
        User user = userRepository.findById(recipientUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
        Set<String> phones = new LinkedHashSet<>();
        String loginNorm = PhoneLoginNormalizer.toLogin(user.getLogin());
        if (!loginNorm.isEmpty()) {
            phones.add(loginNorm);
        }
        if (user.getPhone() != null) {
            String phoneNorm = PhoneLoginNormalizer.toLogin(user.getPhone());
            if (!phoneNorm.isEmpty()) {
                phones.add(phoneNorm);
            }
        }
        if (phones.isEmpty()) {
            return List.of();
        }
        List<GiftCardOrder> orders = giftCardOrderRepository
                .findByRecipientPhoneInAndSenderUserIdNotOrderByCreatedAtDesc(phones, recipientUserId);
        List<GiftCardOrderResponse> out = new ArrayList<>(orders.size());
        for (GiftCardOrder o : orders) {
            out.add(new GiftCardOrderResponse(
                    o.getId(),
                    o.getDesignId(),
                    o.getAmount(),
                    o.getRecipientPhone(),
                    o.getCreatedAt()));
        }
        return out;
    }

    private void creditRecipientGiftBalance(String recipientPhone, BigDecimal amount) {
        if (recipientPhone == null || recipientPhone.isEmpty() || amount == null) {
            return;
        }
        Optional<User> byLogin = userRepository.findByLogin(recipientPhone);
        if (byLogin.isPresent()) {
            addGiftBalance(byLogin.get(), amount);
            return;
        }
        userRepository.findAll().stream()
                .filter(u -> u.getPhone() != null
                        && recipientPhone.equals(PhoneLoginNormalizer.toLogin(u.getPhone())))
                .findFirst()
                .ifPresent(u -> addGiftBalance(u, amount));
    }

    private void addGiftBalance(User user, BigDecimal amount) {
        BigDecimal current = user.getGiftBalance() != null ? user.getGiftBalance() : BigDecimal.ZERO;
        user.setGiftBalance(current.add(amount));
        userRepository.save(user);
    }
}
