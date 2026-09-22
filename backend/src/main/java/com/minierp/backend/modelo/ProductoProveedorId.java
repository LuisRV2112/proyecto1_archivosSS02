package com.minierp.backend.modelo;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

// Llave compuesta de producto_proveedor: la relacion M:N no tiene un id propio,
// se identifica por el par (producto_id, proveedor_id), igual que en la base.
@Embeddable
public class ProductoProveedorId implements Serializable {

    private Integer productoId;
    private Integer proveedorId;

    public ProductoProveedorId() {
    }

    public ProductoProveedorId(Integer productoId, Integer proveedorId) {
        this.productoId = productoId;
        this.proveedorId = proveedorId;
    }

    public Integer getProductoId() {
        return productoId;
    }

    public void setProductoId(Integer productoId) {
        this.productoId = productoId;
    }

    public Integer getProveedorId() {
        return proveedorId;
    }

    public void setProveedorId(Integer proveedorId) {
        this.proveedorId = proveedorId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProductoProveedorId that)) {
            return false;
        }
        return Objects.equals(productoId, that.productoId) && Objects.equals(proveedorId, that.proveedorId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productoId, proveedorId);
    }
}
