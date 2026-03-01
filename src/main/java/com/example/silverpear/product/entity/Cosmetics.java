package com.example.silverpear.product.entity;
import com.example.silverpear.enums.SkinType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "cosmetics")
public class Cosmetics extends Product {

    @Column(name = "prescription")
    private String prescription;

    @Column(name = "skin_type")
    @Enumerated(EnumType.STRING)
    private SkinType skinType;

    @Column(name = "finish")
    private String finish;
}
