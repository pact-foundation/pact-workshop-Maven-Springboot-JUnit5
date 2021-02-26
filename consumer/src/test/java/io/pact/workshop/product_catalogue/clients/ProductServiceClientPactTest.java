package io.pact.workshop.product_catalogue.clients;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import io.pact.workshop.product_catalogue.models.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "ProductService")
class ProductServiceClientPactTest {
  @Autowired
  private ProductServiceClient productServiceClient;

  @Pact(consumer = "ProductCatalogue")
  public RequestResponsePact allProducts(PactDslWithProvider builder) {
    return builder
      .given("products exists")
        .uponReceiving("get all products")
        .path("/products")
        .matchHeader("Authorization", "Bearer [a-zA-Z0-9=\\+/]+", "Bearer AAABd9yHUjI=")
      .willRespondWith()
        .status(200)
        .body(
          new PactDslJsonBody()
            .minArrayLike("products", 1, 2)
              .integerType("id", 9L)
              .stringType("name", "Gem Visa")
              .stringType("type", "CREDIT_CARD")
              .closeObject()
            .closeArray()
        )
      .toPact();
  }

  @Test
  @PactTestFor(pactMethod = "allProducts")
  void testAllProducts(MockServer mockServer) {
    productServiceClient.setBaseUrl(mockServer.getUrl());
    List<Product> products = productServiceClient.fetchProducts().getProducts();
    assertThat(products, hasSize(2));
    assertThat(products.get(0), is(equalTo(new Product(9L, "Gem Visa", "CREDIT_CARD", null, null))));
  }

  @Pact(consumer = "ProductCatalogue")
  public RequestResponsePact singleProduct(PactDslWithProvider builder) {
    return builder
      .given("product with ID 10 exists", "id", 10)
      .uponReceiving("get product with ID 10")
        .path("/product/10")
        .matchHeader("Authorization", "Bearer [a-zA-Z0-9=\\+/]+", "Bearer AAABd9yHUjI=")
      .willRespondWith()
        .status(200)
        .body(
          new PactDslJsonBody()
            .integerType("id", 10L)
            .stringType("name", "28 Degrees")
            .stringType("type", "CREDIT_CARD")
            .stringType("code", "CC_001")
            .stringType("version", "v1")
        )
      .toPact();
  }

  @Test
  @PactTestFor(pactMethod = "singleProduct")
  void testSingleProduct(MockServer mockServer) {
    productServiceClient.setBaseUrl(mockServer.getUrl());
    Product product = productServiceClient.getProductById(10L);
    assertThat(product, is(equalTo(new Product(10L, "28 Degrees", "CREDIT_CARD", "v1", "CC_001"))));
  }

  @Pact(consumer = "ProductCatalogue")
  public RequestResponsePact noProducts(PactDslWithProvider builder) {
    return builder
      .given("no products exists")
      .uponReceiving("get all products")
        .path("/products")
        .matchHeader("Authorization", "Bearer [a-zA-Z0-9=\\+/]+", "Bearer AAABd9yHUjI=")
      .willRespondWith()
        .status(200)
        .body(
          new PactDslJsonBody().array("products")
        )
      .toPact();
  }

  @Test
  @PactTestFor(pactMethod = "noProducts")
  void testNoProducts(MockServer mockServer) {
    productServiceClient.setBaseUrl(mockServer.getUrl());
    ProductServiceResponse products = productServiceClient.fetchProducts();
    assertThat(products.getProducts(), hasSize(0));
  }

  @Pact(consumer = "ProductCatalogue")
  public RequestResponsePact singleProductNotExists(PactDslWithProvider builder) {
    return builder
      .given("product with ID 10 does not exist", "id", 10)
      .uponReceiving("get product with ID 10")
        .path("/product/10")
        .matchHeader("Authorization", "Bearer [a-zA-Z0-9=\\+/]+", "Bearer AAABd9yHUjI=")
      .willRespondWith()
        .status(404)
      .toPact();
  }

  @Test
  @PactTestFor(pactMethod = "singleProductNotExists")
  void testSingleProductNotExists(MockServer mockServer) {
    productServiceClient.setBaseUrl(mockServer.getUrl());
    try {
      productServiceClient.getProductById(10L);
      fail("Expected service call to throw an exception");
    } catch (HttpClientErrorException ex) {
      assertThat(ex.getMessage(), containsString("404 Not Found"));
    }
  }

  @Pact(consumer = "ProductCatalogue")
  public RequestResponsePact noAuthToken(PactDslWithProvider builder) {
    return builder
      .uponReceiving("get all products with no auth token")
        .path("/products")
      .willRespondWith()
        .status(401)
      .toPact();
  }

  @Test
  @PactTestFor(pactMethod = "noAuthToken")
  void testNoAuthToken(MockServer mockServer) {
    productServiceClient.setBaseUrl(mockServer.getUrl());
    try {
      productServiceClient.fetchProducts();
      fail("Expected service call to throw an exception");
    } catch (HttpClientErrorException ex) {
      assertThat(ex.getMessage(), containsString("401 Unauthorized"));
    }
  }

  @Pact(consumer = "ProductCatalogue")
  public RequestResponsePact noAuthToken2(PactDslWithProvider builder) {
    return builder
      .uponReceiving("get product by ID with no auth token")
        .path("/product/10")
      .willRespondWith()
        .status(401)
      .toPact();
  }

  @Test
  @PactTestFor(pactMethod = "noAuthToken2")
  void testNoAuthToken2(MockServer mockServer) {
    productServiceClient.setBaseUrl(mockServer.getUrl());
    try {
      productServiceClient.getProductById(10L);
      fail("Expected service call to throw an exception");
    } catch (HttpClientErrorException ex) {
      assertThat(ex.getMessage(), containsString("401 Unauthorized"));
    }
  }
}
