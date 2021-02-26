package io.pact.workshop.product_service.controllers;

import io.pact.workshop.product_service.products.Product;
import io.pact.workshop.product_service.products.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ProductsController {
  @ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "product not found")
  public static class ProductNotFoundException extends RuntimeException { }

  @Autowired
  private ProductRepository productRepository;

  @GetMapping("/products")
  public ProductsResponse allProducts() {
    return new ProductsResponse((List<Product>) productRepository.findAll());
  }

  @GetMapping("/product/{id}")
  public Product productById(@PathVariable("id") Long id) {
    return productRepository.findById(id).orElseThrow(ProductNotFoundException::new);
  }
}
