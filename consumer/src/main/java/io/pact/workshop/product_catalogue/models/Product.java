package io.pact.workshop.product_catalogue.models;

import lombok.Data;

@Data
public class Product {
  private final Long id;
  private final String name;
  private final String type;
  private final String version;
  private final String code;
}
