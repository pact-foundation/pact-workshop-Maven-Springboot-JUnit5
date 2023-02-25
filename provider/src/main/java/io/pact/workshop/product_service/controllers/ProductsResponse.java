package io.pact.workshop.product_service.controllers;

import io.pact.workshop.product_service.products.Product;
import java.util.List;
import lombok.Data;

@Data
public class ProductsResponse {
  private final List<Product> products;
}
