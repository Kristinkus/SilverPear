package com.example.silverpear.product.entity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import lombok.Data;
import com.example.silverpear.enums.Gender;

@Entity
@Table(name = "products")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "brand", nullable = false)
    private String brand;

    @Column(name = "description", nullable = true)
    private String description;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "purchasePrice", nullable = false)
    private double purchasePrice;

    @Column(name = "salePrice", nullable = false)
    private double salePrice;

    @Column(name = "oldSalePrice", nullable = false)
    private double oldSalePrice;

    @Column(name = "inStock", nullable = false)
    private boolean inStock;

    @Column(name = "productType")
    private String productType;

    @Column(name = "gender")
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "volume")
    private double volume;
/*
    @ManyToMany(name = "product_orders",
                joinColumns = @JoinColumn(name = product_id),
                )
*/
}