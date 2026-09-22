package com.minierp.backend.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "v_alerta_stock")
public class VAlertaStock {

    @Id
    @Column(name = "producto_id")
    private Integer productoId;

    @Column(length = 20)
    private String codigo;

    @Column(length = 100)
    private String nombre;

    @Column(length = 50)
    private String categoria;

    private Long existencia;

    @Column(name = "stock_minimo")
    private Integer stockMinimo;

    private Long faltante;

    private String nivel;

    public VAlertaStock() {
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

    public Long getExistencia() {
        return existencia;
    }

    public Integer getStockMinimo() {
        return stockMinimo;
    }

    public Long getFaltante() {
        return faltante;
    }

    public String getNivel() {
        return nivel;
    }
}
