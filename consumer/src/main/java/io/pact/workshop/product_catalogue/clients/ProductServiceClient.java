package io.pact.workshop.product_catalogue.clients;

import io.pact.workshop.product_catalogue.models.Product;
import java.nio.ByteBuffer;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Service
public class ProductServiceClient {
  private final RestTemplate restTemplate;

  @Value("${serviceClients.products.baseUrl}")
  private String baseUrl;

  public ProductServiceResponse fetchProducts() {
    return callApi("/products", ProductServiceResponse.class);
  }

  public Product getProductById(long id) {
    return callApi("/product/" + id, Product.class);
  }

  private <T> T callApi(String path, Class<T> responseType) {
    HttpHeaders headers = new HttpHeaders();
    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
    buffer.putLong(System.currentTimeMillis());
    headers.setBearerAuth(Base64.getEncoder().encodeToString(buffer.array()));
    HttpEntity<?> requestEntity = new HttpEntity<>(headers);
    return restTemplate
        .exchange(baseUrl + path, HttpMethod.GET, requestEntity, responseType)
        .getBody();
  }
}
