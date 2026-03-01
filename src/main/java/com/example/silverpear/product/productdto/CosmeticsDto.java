package com.example.silverpear.product.productdto;

import com.example.silverpear.enums.SkinType;
import lombok.Data;

@Data
public class CosmeticsDto extends ProductDto {
    private String prescription;
    private SkinType skinType;
    private String finish;
}