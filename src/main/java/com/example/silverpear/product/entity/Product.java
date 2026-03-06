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

    @Column(name = "description")
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


    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<OrderItem> orderItems;


    @ManyToMany(mappedBy = "favorites")
    @JsonIgnore
    private Set<User> favoritedBy;
}