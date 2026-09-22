package com.minierp.backend.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;

// Mapea la vista v_existencia (V3__vistas.sql). Es de solo lectura: @Immutable
// evita que Hibernate intente hacer INSERT/UPDATE/DELETE sobre una vista, que
// fallaria porque no tiene reglas de escritura.
@Entity
@Immutable
@Table(name = "v_existencia")
public class VExistencia {

    @Id
    @Column(name = "producto_id")
    private Integer productoId;

    @Column(length = 20)
    private String codigo;

    @Column(length = 100)
    private String nombre;

    @Column(length = 50)
    private String categoria;

    @Column(name = "precio_venta", precision = 12, scale = 2)
    private BigDecimal precioVenta;

    @Column(name = "stock_minimo")
    private Integer stockMinimo;

    private boolean activo;

    // COALESCE(SUM(...), 0): SUM sobre entero da bigint en PostgreSQL
    private Long existencia;

    // COALESCE(SUM(cantidad * costo), 0): numeric sin precision/escala fija
    @Column(name = "valor_inventario")
    private BigDecimal valorInventario;

    public VExistencia() {
    }

    public Integer getProductoId() {
        return productoId;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCategoria() {
        return categoria;
    }

    public BigDecimal getPrecioVenta() {
        return precioVenta;
    }

    public Integer getStockMinimo() {
        return stockMinimo;
    }

    public boolean isActivo() {
        return activo;
    }

    public Long getExistencia() {
        return existencia;
    }

    public BigDecimal getValorInventario() {
        return valorInventario;
    }
}
