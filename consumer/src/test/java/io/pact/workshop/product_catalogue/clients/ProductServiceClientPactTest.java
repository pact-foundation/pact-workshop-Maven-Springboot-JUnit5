package io.pact.workshop.product_catalogue.clients;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import io.pact.workshop.product_catalogue.models.Product;
import java.util.List;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.HttpClientErrorException;

@SpringBootTest
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "ProductService")
class ProductServiceClientPactTest extends BaseTest {
  @Autowired private ProductServiceClient productServiceClient;

  @Pact(consumer = "ProductCatalogue")
  public RequestResponsePact allProducts(PactDslWithProvider builder) {
    final List<Product> productsWithAllVersions = getProducts().getProducts();

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
                .integerType("id", productsWithAllVersions.get(0).getId())
                .stringType("name", productsWithAllVersions.get(0).getName())
                .stringType("type", productsWithAllVersions.get(0).getType())
                .closeObject()
                .closeArray())
        .toPact();
  }

  @Test
  @PactTestFor(pactMethod = "allProducts")
  void testAllProducts(MockServer mockServer) throws IllegalAccessException {
    FieldUtils.writeField(productServiceClient, "baseUrl", mockServer.getUrl(), true);

    final List<Product> productsWithAllVersions = getProducts().getProducts();

    List<Product> products = productServiceClient.fetchProducts().getProducts();

    assertThat(products, hasSize(productsWithAllVersions.size()));
    assertThat(
        products.get(0),
        is(
            equalTo(
                new Product(
                    productsWithAllVersions.get(0).getId(),
                    productsWithAllVersions.get(0).getName(),
                    productsWithAllVersions.get(0).getType(),
                    null,
                    null))));
  }

  @Pact(consumer = "ProductCatalogue")
  public RequestResponsePact singleProduct(PactDslWithProvider builder) {
    final Product productWithV1 = getProductWithV1Version();

    return builder
        .given(
            String.format("product with ID %d exists", productWithV1.getId()),
            "id",
            productWithV1.getId())
        .uponReceiving(String.format("get product with ID %d", productWithV1.getId()))
        .path("/product/" + productWithV1.getId())
        .matchHeader("Authorization", "Bearer [a-zA-Z0-9=\\+/]+", "Bearer AAABd9yHUjI=")
        .willRespondWith()
        .status(200)
        .body(
            new PactDslJsonBody()
                .integerType("id", productWithV1.getId())
                .stringType("name", productWithV1.getName())
                .stringType("type", productWithV1.getType())
                .stringType("code", productWithV1.getCode())
                .stringType("version", productWithV1.getVersion()))
        .toPact();
  }

  @Test
  @PactTestFor(pactMethod = "singleProduct")
  void testSingleProduct(MockServer mockServer) throws IllegalAccessException {
    FieldUtils.writeField(productServiceClient, "baseUrl", mockServer.getUrl(), true);

    final Product productWithV1 = getProductWithV1Version();

    Product product = productServiceClient.getProductById(productWithV1.getId());
    assertThat(
        product,
        is(
            equalTo(
                new Product(
                    productWithV1.getId(),
                    productWithV1.getName(),
                    productWithV1.getType(),
                    productWithV1.getVersion(),
                    productWithV1.getCode()))));
  }

  @Pact(consumer = "ProductCatalogue")
  public RequestResponsePact noProducts(PactDslWithProvider builder) {
    return builder
        .given("no products exists")
        .uponReceiving("get empty list of products")
        .path("/products")
        .matchHeader("Authorization", "Bearer [a-zA-Z0-9=\\+/]+", "Bearer AAABd9yHUjI=")
        .willRespondWith()
        .status(200)
        .body(new PactDslJsonBody().array("products"))
        .toPact();
  }

  @Test
  @PactTestFor(pactMethod = "noProducts")
  void testNoProducts(MockServer mockServer) throws IllegalAccessException {
    FieldUtils.writeField(productServiceClient, "baseUrl", mockServer.getUrl(), true);
    ProductServiceResponse products = productServiceClient.fetchProducts();
    assertThat(products.getProducts(), hasSize(0));
  }

  @Pact(consumer = "ProductCatalogue")
  public RequestResponsePact singleProductNotExists(PactDslWithProvider builder) {
    final Product productWithV1 = getProductWithV1Version();

    return builder
        .given(
            String.format("product with ID %d does not exist", productWithV1.getId()),
            "id",
            productWithV1.getId())
        .uponReceiving(String.format("get Not Found status"))
        .path("/product/" + productWithV1.getId())
        .matchHeader("Authorization", "Bearer [a-zA-Z0-9=\\+/]+", "Bearer AAABd9yHUjI=")
        .willRespondWith()
        .status(404)
        .toPact();
  }

  @Test
  @PactTestFor(pactMethod = "singleProductNotExists")
  void testSingleProductNotExists(MockServer mockServer) throws IllegalAccessException {
    final Product productWithV1 = getProductWithV1Version();

    FieldUtils.writeField(productServiceClient, "baseUrl", mockServer.getUrl(), true);
    try {
      productServiceClient.getProductById(productWithV1.getId());
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
  void testNoAuthToken(MockServer mockServer) throws IllegalAccessException {
    FieldUtils.writeField(productServiceClient, "baseUrl", mockServer.getUrl(), true);
    try {
      productServiceClient.fetchProducts();
      fail("Expected service call to throw an exception");
    } catch (HttpClientErrorException ex) {
      assertThat(ex.getMessage(), containsString("401 Unauthorized"));
    }
  }

  @Pact(consumer = "ProductCatalogue")
  public RequestResponsePact noAuthToken2(PactDslWithProvider builder) {
    final Product productWithV1 = getProductWithV1Version();
    return builder
        .uponReceiving("get product by ID with no auth token")
        .path("/product/" + productWithV1.getId())
        .willRespondWith()
        .status(401)
        .toPact();
  }

  @Test
  @PactTestFor(pactMethod = "noAuthToken2")
  void testNoAuthToken2(MockServer mockServer) throws IllegalAccessException {
    FieldUtils.writeField(productServiceClient, "baseUrl", mockServer.getUrl(), true);

    final Product productWithV1 = getProductWithV1Version();
    try {
      productServiceClient.getProductById(productWithV1.getId());
      fail("Expected service call to throw an exception");
    } catch (HttpClientErrorException ex) {
      assertThat(ex.getMessage(), containsString("401 Unauthorized"));
    }
  }
}
