package com.minierp.backend.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

import java.math.BigDecimal;

// Relacion muchos a muchos: un proveedor surte varios productos y un producto
// puede comprarse a varios proveedores. Una compra solo puede incluir lineas
// que existan aqui (regla 5.3 del CLAUDE.md).
@Entity
@Table(name = "producto_proveedor")
public class ProductoProveedor {

    @EmbeddedId
    private ProductoProveedorId id = new ProductoProveedorId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("productoId")
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("proveedorId")
    @JoinColumn(name = "proveedor_id")
    private Proveedor proveedor;

    @Column(name = "costo_referencia", precision = 12, scale = 2)
    private BigDecimal costoReferencia;

    public ProductoProveedor() {
    }

    public ProductoProveedorId getId() {
        return id;
    }

    public void setId(ProductoProveedorId id) {
        this.id = id;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
    }

    public BigDecimal getCostoReferencia() {
        return costoReferencia;
    }

    public void setCostoReferencia(BigDecimal costoReferencia) {
        this.costoReferencia = costoReferencia;
    }
}
