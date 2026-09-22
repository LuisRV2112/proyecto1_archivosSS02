package com.minierp.backend.repositorio;

import com.minierp.backend.modelo.ProductoProveedor;
import com.minierp.backend.modelo.ProductoProveedorId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoProveedorRepositorio extends JpaRepository<ProductoProveedor, ProductoProveedorId> {

    // Valida la regla de la Fase 4: una compra solo puede incluir productos
    // que ese proveedor surte
    boolean existsById_ProductoIdAndId_ProveedorId(Integer productoId, Integer proveedorId);

    // Productos que surte un proveedor, con su costo de referencia (Fase 3)
    List<ProductoProveedor> findById_ProveedorId(Integer proveedorId);
}
