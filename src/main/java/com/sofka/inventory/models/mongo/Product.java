package com.sofka.inventory.models.mongo;

import org.springframework.data.annotation.Id;

import java.util.Objects;

public class Product {
    @Id
    private String id;

    private String code;
    private String description;
    private Boolean enabled;
    private Integer quantity;

    public Product() {
    }

    public Product(String id, String code, String description, Boolean enabled, Integer quantity) {
        this.id = id;
        this.code = code;
        this.description = description;
        this.enabled = enabled;
        this.quantity = quantity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(code, product.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }
}
