package io.pact.workshop.product_service.products;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {
  @Id
  private Long id;
  private String name;
  private String type;
  private String version;
  private String code;
}
