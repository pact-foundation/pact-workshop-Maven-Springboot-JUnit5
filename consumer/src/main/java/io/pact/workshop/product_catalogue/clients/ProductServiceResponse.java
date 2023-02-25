package io.pact.workshop.product_catalogue.clients;

import io.pact.workshop.product_catalogue.models.Product;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class ProductServiceResponse {
  private List<Product> products;

  public ProductServiceResponse(List<Product> products) {
    super();
    this.products = products;
  }
}
