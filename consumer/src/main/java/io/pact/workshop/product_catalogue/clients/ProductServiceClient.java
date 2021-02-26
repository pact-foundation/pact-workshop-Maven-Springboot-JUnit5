package io.pact.workshop.product_catalogue.clients;

import io.pact.workshop.product_catalogue.models.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.ByteBuffer;
import java.util.Base64;

@Service
public class ProductServiceClient {
  @Autowired
  private RestTemplate restTemplate;

  @Value("${serviceClients.products.baseUrl}")
  private String baseUrl;

  public ProductServiceResponse fetchProducts() {
    return callApi("/products", ProductServiceResponse.class);
  }

  public Product getProductById(long id) {
    return callApi("/product/" + id, Product.class);
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  private <T> T callApi(String path, Class<T> responseType) {
    HttpHeaders headers = new HttpHeaders();
    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
    buffer.putLong(System.currentTimeMillis());
    headers.setBearerAuth(Base64.getEncoder().encodeToString(buffer.array()));
    HttpEntity<?> requestEntity = new HttpEntity<>(headers);
    return restTemplate.exchange(baseUrl + path, HttpMethod.GET, requestEntity, responseType).getBody();
  }
}
