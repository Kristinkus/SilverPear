package com.example.silverpear.product.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String login;

    @Column(nullable = false)
    private String password;

    @Column
    private String name;

    @Column
    private String surname;

    @Column
    private String email;

    @Column
    private String phone;

    // OneToMany связь: один пользователь - много заказов
    // CascadeType.ALL - при сохранении/удалении пользователя сохраняются/удаляются и его заказы
    // FetchType.LAZY - заказы загружаются только при необходимости (решение проблемы производительности)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Order> orders = new ArrayList<>();

    // Вспомогательный метод для добавления заказа
    public void addOrder(Order order) {
        orders.add(order);
        order.setUser(this);
    }

    // Вспомогательный метод для удаления заказа
    public void removeOrder(Order order) {
        orders.remove(order);
        order.setUser(null);
    }
}