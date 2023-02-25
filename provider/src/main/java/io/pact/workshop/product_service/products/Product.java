package io.pact.workshop.product_service.products;

import javax.persistence.Entity;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {
  @Id private Long id;
  private String name;
  private String type;
  private String version;
  private String code;
}
