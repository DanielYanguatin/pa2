package com.example.productoscrud.infrastructure.persistence.mongo;

import com.example.productoscrud.application.ports.ProductRepository;
import com.example.productoscrud.domain.model.MongoAggregateResponse;
import com.example.productoscrud.domain.model.Product;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementaci\u00f3n de persistencia en MongoDB (NoSQL).
 * Hereda del mismo contrato abstracto {@link ProductRepository} que usa MySQL,
 * respetando el desacoplamiento de la arquitectura hexagonal.
 * Adem\u00e1s de las 5 operaciones b\u00e1sicas de CRUD, expone 4 m\u00e9todos de
 * listado filtrado mediante pipelines de aggregate de MongoDB.
 */
@Repository("mongoRepository")
public class ProductRepositoryMongo extends ProductRepository {

    private final MongoTemplate mongoTemplate;
    private static final String COLECCION = "products";

    public ProductRepositoryMongo(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /* ------------------------------------------------------------------ *
     *  Operaciones b\u00e1sicas de CRUD (mismas 5 que la versi\u00f3n SQL)   *
     * ------------------------------------------------------------------ */

    @Override
    public List<Product> listar() {
        List<Document> documentos = mongoTemplate.findAll(Document.class, COLECCION);
        return toProductos(documentos);
    }

    @Override
    public Optional<Product> buscarPorId(int id) {
        Query consulta = new Query(Criteria.where("id").is(id));
        Document documento = mongoTemplate.findOne(consulta, Document.class, COLECCION);
        return Optional.ofNullable(documento).map(this::documentToProducto);
    }

    @Override
    public Product crear(Product product) {
        Integer id = obtenerSiguienteId();
        Document documento = new Document("_id", id)
                .append("id", id)
                .append("name", product.getName())
                .append("price", product.getPrice().doubleValue())
                .append("description", product.getDescription());
        mongoTemplate.insert(documento, COLECCION);
        return new Product(id, product.getName(), product.getPrice(), product.getDescription());
    }

    @Override
    public boolean actualizar(int id, Product product) {
        Query consulta = new Query(Criteria.where("id").is(id));
        Update actualizacion = new Update()
                .set("name", product.getName())
                .set("price", product.getPrice().doubleValue())
                .set("description", product.getDescription());
        return mongoTemplate.updateFirst(consulta, actualizacion, Document.class, COLECCION).getModifiedCount() > 0;
    }

    @Override
    public boolean eliminar(int id) {
        Query consulta = new Query(Criteria.where("id").is(id));
        return mongoTemplate.remove(consulta, Document.class, COLECCION).getDeletedCount() > 0;
    }

    /* ------------------------------------------------------------------ *
     *  4 m\u00e9todos de listado filtrado con pipelines de aggregate       *
     * ------------------------------------------------------------------ */

    /**
     * 1. Listar productos cuyo precio sea mayor o igual al valor indicado,
     *    ordenados de menor a mayor precio.
     */
    public MongoAggregateResponse listarPorPrecioMayorIgual(BigDecimal minimo) {
        List<Bson> pipeline = new ArrayList<>();
        pipeline.add(Aggregates.match(Filters.gte("price", minimo.doubleValue())));
        pipeline.add(Aggregates.sort(Sorts.ascending("price")));

        String pipelineJson = pipelinePrecioMayorIgualJson(minimo);
        List<Product> resultados = ejecutarAggregate(pipeline);
        return new MongoAggregateResponse(resultados, pipelineJson);
    }

    /**
     * 2. Listar productos cuyo nombre contenga el texto indicado (case insensitive).
     */
    public MongoAggregateResponse listarPorNombreContiene(String texto) {
        List<Bson> pipeline = new ArrayList<>();
        pipeline.add(Aggregates.match(Filters.regex("name", texto, "i")));

        String pipelineJson = pipelineNombreContieneJson(texto);
        List<Product> resultados = ejecutarAggregate(pipeline);
        return new MongoAggregateResponse(resultados, pipelineJson);
    }

    /**
     * 3. Listar productos cuyo descripci\u00f3n contenga el texto indicado
     *    (case insensitive).
     */
    public MongoAggregateResponse listarPorDescripcionContiene(String texto) {
        List<Bson> pipeline = new ArrayList<>();
        pipeline.add(Aggregates.match(Filters.regex("description", texto, "i")));

        String pipelineJson = pipelineDescripcionContieneJson(texto);
        List<Product> resultados = ejecutarAggregate(pipeline);
        return new MongoAggregateResponse(resultados, pipelineJson);
    }

    /**
     * 4. Listar todos los productos ordenados de mayor a menor precio.
     */
    public MongoAggregateResponse listarOrdenadosPorPrecioDescendente() {
        List<Bson> pipeline = new ArrayList<>();
        pipeline.add(Aggregates.sort(Sorts.descending("price")));

        String pipelineJson = pipelinePrecioDescendenteJson();
        List<Product> resultados = ejecutarAggregate(pipeline);
        return new MongoAggregateResponse(resultados, pipelineJson);
    }

    /* ------------------------------------------------------------------ *
     *  M\u00e9todos privados de apoyo                                       *
     * ------------------------------------------------------------------ */

    private List<Product> ejecutarAggregate(List<Bson> pipeline) {
        List<Document> documentos = mongoTemplate.getCollection(COLECCION)
                .aggregate(pipeline)
                .into(new ArrayList<>());
        return toProductos(documentos);
    }

    private List<Product> toProductos(List<Document> documentos) {
        List<Product> productos = new ArrayList<>();
        for (Document doc : documentos) {
            productos.add(documentToProducto(doc));
        }
        return productos;
    }

    private Product documentToProducto(Document doc) {
        return new Product(
                doc.getInteger("id"),
                doc.getString("name"),
                BigDecimal.valueOf(doc.getDouble("price")),
                doc.getString("description")
        );
    }

    private Integer obtenerSiguienteId() {
        Query consulta = new Query().with(Sort.by(Sort.Direction.DESC, "id")).limit(1);
        List<Document> ultimos = mongoTemplate.find(consulta, Document.class, COLECCION);
        if (ultimos.isEmpty()) {
            return 1;
        }
        return ultimos.get(0).getInteger("id") + 1;
    }

    /* ------------------------------------------------------------------ *
     *  C\u00f3digos JSON de los pipelines (para mostrar en el frontend)    *
     * ------------------------------------------------------------------ */

    private String pipelinePrecioMayorIgualJson(BigDecimal minimo) {
        return "[{\"$match\":{\"price\":{\"$gte\":" + minimo.stripTrailingZeros().toPlainString() + "}}},{\"$sort\":{\"price\":1}}]";
    }

    private String pipelineNombreContieneJson(String texto) {
        return "[{\"$match\":{\"name\":{\"$regex\":\"" + escaparJson(texto) + "\",\"$options\":\"i\"}}}]";
    }

    private String pipelineDescripcionContieneJson(String texto) {
        return "[{\"$match\":{\"description\":{\"$regex\":\"" + escaparJson(texto) + "\",\"$options\":\"i\"}}}]";
    }

    private String pipelinePrecioDescendenteJson() {
        return "[{\"$sort\":{\"price\":-1}}]";
    }

    private String escaparJson(String texto) {
        return texto.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
