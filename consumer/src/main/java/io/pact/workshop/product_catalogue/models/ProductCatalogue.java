package io.pact.workshop.product_catalogue.models;

import java.util.List;
import lombok.Data;

@Data
public class ProductCatalogue {
  private final String name;
  private final List<Product> products;
}
