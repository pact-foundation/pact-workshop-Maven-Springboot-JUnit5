package io.pact.workshop.product_catalogue.controllers;

import io.pact.workshop.product_catalogue.clients.ProductServiceClient;
import io.pact.workshop.product_catalogue.models.ProductCatalogue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductCatalogueController {
  @Autowired
  private ProductServiceClient productServiceClient;

  @GetMapping("/catalogue")
  public ProductCatalogue catalogue() {
    return new ProductCatalogue("Default Catalogue", productServiceClient.fetchProducts().getProducts());
  }
}
