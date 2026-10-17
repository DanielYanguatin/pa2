package com.example.productoscrud.application.ports;

import com.example.productoscrud.domain.model.Product;
import java.util.List;
import java.util.Optional;

public abstract class ProductRepository {
    public abstract List<Product> listar();
    public abstract Optional<Product> buscarPorId(int id);
    public abstract Product crear(Product product);
    public abstract boolean actualizar(int id, Product product);
    public abstract boolean eliminar(int id);
}

// Origen: ProductService (com.example.productoscrud.application.service) llama a este contrato.
// Destino: Java ejecuta los métodos sobrescritos en ProductRepositoryMySQL
// (com.example.productoscrud.infrastructure.persistence.mysql), que extiende esta clase.
