package io.pact.workshop.product_service;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.StateChangeAction;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.spring.junit5.PactVerificationSpringProvider;
import io.pact.workshop.product_service.products.Product;
import io.pact.workshop.product_service.products.ProductRepository;
import org.apache.http.HttpRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Provider("ProductService")
@PactBroker
public class PactVerificationTest {
  @LocalServerPort
  private int port;

  @Autowired
  private ProductRepository productRepository;

  @BeforeEach
  void setup(PactVerificationContext context) {
    context.setTarget(new HttpTestTarget("localhost", port));
  }

  @TestTemplate
  @ExtendWith(PactVerificationSpringProvider.class)
  void pactVerificationTestTemplate(PactVerificationContext context, HttpRequest request) {
    // WARNING: Do not modify anything else on the request, because you could invalidate the contract
    if (request.containsHeader("Authorization")) {
      request.setHeader("Authorization", "Bearer " + generateToken());
    }
    context.verifyInteraction();
  }

  private static String generateToken() {
    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
    buffer.putLong(System.currentTimeMillis());
    return Base64.getEncoder().encodeToString(buffer.array());
  }

  @State(value = "products exists", action = StateChangeAction.SETUP)
  void productsExists() {
    productRepository.deleteAll();
    productRepository.saveAll(Arrays.asList(
      new Product(100L, "Test Product 1", "CREDIT_CARD", "v1", "CC_001"),
      new Product(200L, "Test Product 2", "CREDIT_CARD", "v1", "CC_002"),
      new Product(300L, "Test Product 3", "PERSONAL_LOAN", "v1", "PL_001"),
      new Product(400L, "Test Product 4", "SAVINGS", "v1", "SA_001")
    ));
  }

  @State(value = "no products exists", action = StateChangeAction.SETUP)
  void noProductsExist() {
    productRepository.deleteAll();
  }

  @State(value = "product with ID 10 exists", action = StateChangeAction.SETUP)
  void productExists(Map<String, Object> params) {
    long productId = ((Number) params.get("id")).longValue();
    Optional<Product> product = productRepository.findById(productId);
    if (!product.isPresent()) {
      productRepository.save(new Product(productId, "Product", "TYPE", "v1", "001"));
    }
  }

  @State(value = "product with ID 10 does not exist", action = StateChangeAction.SETUP)
  void productNotExist(Map<String, Object> params) {
    long productId = ((Number) params.get("id")).longValue();
    Optional<Product> product = productRepository.findById(productId);
    if (product.isPresent()) {
      productRepository.deleteById(productId);
    }
  }
}
