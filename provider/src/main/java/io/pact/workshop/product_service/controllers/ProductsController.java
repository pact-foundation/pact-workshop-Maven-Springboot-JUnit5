package io.pact.workshop.product_service.controllers;

import io.pact.workshop.product_service.products.Product;
import io.pact.workshop.product_service.products.ProductRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class ProductsController {

  private final ProductRepository productRepository;

  @GetMapping("/products")
  public ProductsResponse allProducts() {
    final var allProducts = new ProductsResponse((List<Product>) productRepository.findAll());
    return allProducts;
  }

  @GetMapping("/product/{id}")
  public Product productById(@PathVariable("id") Long id) {
    final var product = productRepository.findById(id).orElseThrow(ProductNotFoundException::new);
    return product;
  }

  @ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "product not found")
  public static class ProductNotFoundException extends RuntimeException {
    private static final long serialVersionUID = -1658284135693252129L;
  }
}
