package io.pact.workshop.product_catalogue.clients;

import io.pact.workshop.product_catalogue.models.Product;
import java.util.List;

public abstract class BaseTest {

  protected static ProductServiceResponse getProducts() {
    return new ProductServiceResponse(
        List.of(getProductWithV2Version(), getProductWithV1Version()));
  }

  protected static Product getProductWithV1Version() {
    return getProduct(10l, "28 Degrees", "CREDIT_CARD", "v1", "CC_001");
  }

  protected static Product getProductWithV2Version() {
    return getProduct(9l, "Gem Visa", "CREDIT_CARD", "v2", null);
  }

  private static Product getProduct(
      final Long id,
      final String name,
      final String type,
      final String version,
      final String code) {
    return new Product(id, name, type, version, code);
  }
}
