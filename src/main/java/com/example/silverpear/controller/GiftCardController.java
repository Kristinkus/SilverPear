package com.example.silverpear.controller;

import com.example.silverpear.product.productdto.GiftCardOrderRequest;
import com.example.silverpear.product.productdto.GiftCardOrderResponse;
import com.example.silverpear.security.AuthPrincipal;
import com.example.silverpear.service.GiftCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/gift-cards")
@RequiredArgsConstructor
public class GiftCardController {

    private final GiftCardService giftCardService;
    private final AuthPrincipal authPrincipal;

    @GetMapping("/received")
    public List<GiftCardOrderResponse> listReceived() {
        Long userId = authPrincipal.currentUser().getId();
        return giftCardService.listReceivedByRecipient(userId);
    }

    @PostMapping
    public ResponseEntity<GiftCardOrderResponse> create(@Valid @RequestBody GiftCardOrderRequest request) {
        Long userId = authPrincipal.currentUser().getId();
        GiftCardOrderResponse body = giftCardService.create(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }
}
