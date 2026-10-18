package com.example.productoscrud.application.service;

import com.example.productoscrud.domain.model.MongoAggregateResponse;
import com.example.productoscrud.domain.model.Product;
import com.example.productoscrud.infrastructure.persistence.mongo.ProductRepositoryMongo;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Casos de uso para la persistencia NoSQL (MongoDB).
 * Conocen el repositorio concreto de Mongo porque los m\u00e9todos de agregaci\u00f3n
 * son espec\u00edficos de esa tecnolog\u00eda y no pertenecen al contrato abstracto
 * com\u00fan de CRUD.
 */
public class ProductMongoService {

    private final ProductRepositoryMongo productRepositoryMongo;

    public ProductMongoService(ProductRepositoryMongo productRepositoryMongo) {
        this.productRepositoryMongo = productRepositoryMongo;
    }

    public List<Product> listar() {
        return productRepositoryMongo.listar();
    }

    public Optional<Product> buscarPorId(int id) {
        return productRepositoryMongo.buscarPorId(id);
    }

    public Product crear(Product product) {
        return productRepositoryMongo.crear(product);
    }

    public Optional<Product> actualizar(int id, Product product) {
        if (!productRepositoryMongo.actualizar(id, product)) {
            return Optional.empty();
        }
        return productRepositoryMongo.buscarPorId(id);
    }

    public boolean eliminar(int id) {
        return productRepositoryMongo.eliminar(id);
    }

    /* M\u00e9todos espec\u00edficos de MongoDB (aggregate) */

    public MongoAggregateResponse listarPorPrecioMayorIgual(BigDecimal minimo) {
        return productRepositoryMongo.listarPorPrecioMayorIgual(minimo);
    }

    public MongoAggregateResponse listarPorNombreContiene(String texto) {
        return productRepositoryMongo.listarPorNombreContiene(texto);
    }

    public MongoAggregateResponse listarPorDescripcionContiene(String texto) {
        return productRepositoryMongo.listarPorDescripcionContiene(texto);
    }

    public MongoAggregateResponse listarOrdenadosPorPrecioDescendente() {
        return productRepositoryMongo.listarOrdenadosPorPrecioDescendente();
    }
}
