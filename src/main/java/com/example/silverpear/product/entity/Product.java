package com.example.silverpear.product.entity;

import com.example.silverpear.enums.Gender;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Entity
@Table(name = "products")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "brand", nullable = false)
    private String brand;

    /** LONGTEXT вне лимита строки InnoDB (в отличие от VARCHAR(16000), см. ошибку «Row size too large»). */
    @Column(name = "description", columnDefinition = "LONGTEXT")
    private String description;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "sale_price", nullable = false)
    private double salePrice;

    @Column(name = "in_stock", nullable = false)
    private boolean inStock;

    /** Остаток на складе (штуки). Для витрины «успей купить»: остаток меньше 5. */
    @Column(name = "stock_quantity", nullable = false, columnDefinition = "INTEGER NOT NULL DEFAULT 0")
    private int stockQuantity;

    @Column(name = "type")
    private String type;

    @Column(name = "gender")
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "volume")
    private double volume;

    /** Путь к картинке для витрины, например {@code /products/sku-123.jpg} (статика из {@code classpath:/static}). */
    @Column(name = "image_url", length = 512)
    private String imageUrl;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<OrderItem> orderItems;


    @ManyToMany(mappedBy = "favorites")
    @JsonIgnore
    private Set<User> favoritedBy;
}