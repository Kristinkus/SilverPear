package com.example.silverpear.controller;

import com.example.silverpear.product.entity.Product;
import com.example.silverpear.product.productDto.UserDto;
import com.example.silverpear.product.productDto.UserMapper;
import com.example.silverpear.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/shop")
@RequiredArgsConstructor

public class ProductController {

    private final ProductService productService;
    private final UserMapper userMapper;

    @RequestMapping("/products")
    public List<String> getAllProducts(){
        return List.of("Парфюм", "Косметика");
    }

    @RequestMapping(value = "/products/{id}")
    public String getProduct(@PathVariable(value = "id")  int id){
        return "";
    }

    @GetMapping("/details")
    public String getDetails(@RequestParam(value = "param1", required = true, defaultValue = "") String param1,
                             @RequestParam(value = "param2", defaultValue = "") String param2){

        return param1+param2;
    }

    @GetMapping("/api/products")
    public List<UserDto> getAllProductsApi() {
        List<Product> products = productService.getAllProducts();
        return userMapper.toDtoList(products);
    }

    @GetMapping("/api/products/{id}")
    public UserDto getProductById(@PathVariable int id) {
        Product product = productService.getProductById(id);
        return userMapper.toDto(product);
    }

    @PostMapping("/api/products")
    public UserDto createProduct(@RequestBody UserDto productDto) {
        Product product = userMapper.toEntity(productDto);
        Product savedProduct = productService.createProduct(product);
        return userMapper.toDto(savedProduct);
    }

    @GetMapping("/api/products/search")
    public List<UserDto> searchProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String category) {

        List<Product> products = productService.searchProducts(name, brand, category);
        return userMapper.toDtoList(products);
    }



}
