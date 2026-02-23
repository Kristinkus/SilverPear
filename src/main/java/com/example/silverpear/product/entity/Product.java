package com.example.silverpear.product.entity;
import lombok.Data;
@Data
public class Product {

    private int id;
    private String name;
    private double price;
    private String brand;
    private String category;
    private double purchasePrice;

    public double overprice(){
        return purchasePrice*1.5;
    }

}