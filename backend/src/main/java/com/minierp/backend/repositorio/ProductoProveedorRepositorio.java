package com.minierp.backend.repositorio;

import com.minierp.backend.modelo.ProductoProveedor;
import com.minierp.backend.modelo.ProductoProveedorId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoProveedorRepositorio extends JpaRepository<ProductoProveedor, ProductoProveedorId> {

    boolean existsById_ProductoIdAndId_ProveedorId(Integer productoId, Integer proveedorId);

    List<ProductoProveedor> findById_ProveedorId(Integer proveedorId);
}
