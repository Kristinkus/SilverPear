package com.example.silverpear.product.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "perfume")
public class Perfume extends Product {

    @Column(name = "top_notes")
    private List<String> topNotes;

    @Column(name = "middle_notes")
    private List<String> middleNotes;

    @Column(name = "base_notes")
    private List<String> baseNotes;

}
