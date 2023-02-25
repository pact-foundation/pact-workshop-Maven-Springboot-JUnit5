package io.pact.workshop.product_catalogue.controllers;

import io.pact.workshop.product_catalogue.clients.ProductServiceClient;
import io.pact.workshop.product_catalogue.models.ProductCatalogue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RequiredArgsConstructor
@Controller
public class ProductCatalogueController {
  private final ProductServiceClient productServiceClient;

  @GetMapping("/catalogue")
  public String catalogue(Model model) {
    final var catalogue =
        new ProductCatalogue(
            "Default Catalogue", productServiceClient.fetchProducts().getProducts());
    model.addAttribute("catalogue", catalogue);
    return "catalogue";
  }

  @GetMapping("/catalogue/{id}")
  public String catalogue(@PathVariable("id") Long id, Model model) {
    model.addAttribute("product", productServiceClient.getProductById(id));
    return "details";
  }
}
