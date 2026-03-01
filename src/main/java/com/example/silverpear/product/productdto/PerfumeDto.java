package com.example.silverpear.product.productdto;


import lombok.Data;

import java.util.List;

@Data

public class PerfumeDto extends ProductDto {

    private List<String> topNotes;
    private List<String> middleNotes;
    private List<String> baseNotes;
}
