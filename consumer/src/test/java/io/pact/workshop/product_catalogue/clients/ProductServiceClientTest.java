package io.pact.workshop.product_catalogue.clients;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.pact.workshop.product_catalogue.models.Product;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.lanwen.wiremock.ext.WiremockResolver;
import ru.lanwen.wiremock.ext.WiremockResolver.Wiremock;
import ru.lanwen.wiremock.ext.WiremockUriResolver;
import ru.lanwen.wiremock.ext.WiremockUriResolver.WiremockUri;

@SpringBootTest
@ExtendWith({WiremockResolver.class, WiremockUriResolver.class})
class ProductServiceClientTest extends BaseTest {
  @Autowired private ProductServiceClient productServiceClient;
  @Autowired private ObjectMapper objectMapper;

  @Test
  void fetchProducts(@Wiremock WireMockServer server, @WiremockUri String uri)
      throws JsonProcessingException, IllegalAccessException {
    FieldUtils.writeField(productServiceClient, "baseUrl", uri, true);
    final ProductServiceResponse allProducts = getProducts();
    final String jsonProducts = objectMapper.writeValueAsString(allProducts);
    server.stubFor(
        get(urlPathEqualTo("/products"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withBody(jsonProducts)
                    .withHeader("Content-Type", "application/json")));

    final List<Product> productsWithAllVersions = allProducts.getProducts();
    ProductServiceResponse response = productServiceClient.fetchProducts();
    assertThat(response.getProducts(), hasSize(productsWithAllVersions.size()));
    assertThat(
        response.getProducts().stream().map(Product::getId).collect(Collectors.toSet()),
        is(
            equalTo(
                new HashSet<>(
                    Arrays.asList(
                        productsWithAllVersions.get(0).getId(),
                        productsWithAllVersions.get(1).getId())))));
  }

  @Test
  void getProductById(@Wiremock WireMockServer server, @WiremockUri String uri)
      throws JsonProcessingException, IllegalAccessException {
    FieldUtils.writeField(productServiceClient, "baseUrl", uri, true);
    final Product productWithV1 = getProductWithV1Version();
    final String jsonProduct = objectMapper.writeValueAsString(productWithV1);
    server.stubFor(
        get(urlPathEqualTo("/product/" + productWithV1.getId()))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withBody(jsonProduct)
                    .withHeader("Content-Type", "application/json")));

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
}
