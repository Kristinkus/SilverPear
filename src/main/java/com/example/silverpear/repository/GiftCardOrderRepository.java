package com.example.silverpear.repository;

import com.example.silverpear.product.entity.GiftCardOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface GiftCardOrderRepository extends JpaRepository<GiftCardOrder, Long> {

    List<GiftCardOrder> findByRecipientPhoneInAndSenderUserIdNotOrderByCreatedAtDesc(
            Collection<String> recipientPhones,
            Long senderUserId);
}
