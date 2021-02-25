package io.pact.workshop.product_service.controllers;

import io.pact.workshop.product_service.products.Product;
import lombok.Data;

import java.util.List;

@Data
public class ProductsResponse {
  private final List<Product> products;
}
