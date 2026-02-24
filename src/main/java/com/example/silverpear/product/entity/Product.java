package com.example.silverpear.product.entity;
import lombok.Data;

@Data
public class Product {
    private int id;
    private String name;
    private String brand;
    private String category;
    private double purchasePrice;
    private double salePrice;
}