package com.example.productoscrud.infrastructure.web.controllers;

import com.example.productoscrud.application.service.ProductMongoService;
import com.example.productoscrud.domain.model.MongoAggregateResponse;
import com.example.productoscrud.domain.model.Product;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/productos-mongo")
@CrossOrigin(origins = "*")
public class ProductMongoController {

    private final ProductMongoService productMongoService;

    public ProductMongoController(ProductMongoService productMongoService) {
        this.productMongoService = productMongoService;
    }

    /* ---------------- CRUD b\u00e1sico (5 operaciones) ---------------- */

    @GetMapping({"", "/"})
    public List<Product> listar() {
        return productMongoService.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> buscarPorId(@PathVariable int id) {
        return productMongoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping({"", "/"})
    public ResponseEntity<?> crear(@RequestBody Product product) {
        if (!product.esValido()) {
            return ResponseEntity.badRequest().body("Nombre, precio o descripci\u00f3n inv\u00e1lidos");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(productMongoService.crear(product));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable int id, @RequestBody Product product) {
        if (!product.esValido()) {
            return ResponseEntity.badRequest().body("Nombre, precio o descripci\u00f3n inv\u00e1lidos");
        }
        return productMongoService.actualizar(id, product)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable int id) {
        return productMongoService.eliminar(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    /* ---------------- 4 filtros con aggregate de MongoDB ---------------- */

    @GetMapping("/filtro/precio-minimo")
    public MongoAggregateResponse listarPorPrecioMayorIgual(
            @RequestParam BigDecimal minimo) {
        return productMongoService.listarPorPrecioMayorIgual(minimo);
    }

    @GetMapping("/filtro/nombre")
    public MongoAggregateResponse listarPorNombreContiene(
            @RequestParam String texto) {
        return productMongoService.listarPorNombreContiene(texto);
    }

    @GetMapping("/filtro/descripcion")
    public MongoAggregateResponse listarPorDescripcionContiene(
            @RequestParam String texto) {
        return productMongoService.listarPorDescripcionContiene(texto);
    }

    @GetMapping("/filtro/precio-descendente")
    public MongoAggregateResponse listarOrdenadosPorPrecioDescendente() {
        return productMongoService.listarOrdenadosPorPrecioDescendente();
    }
}
