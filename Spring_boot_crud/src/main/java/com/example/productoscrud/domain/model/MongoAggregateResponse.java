package com.example.productoscrud.domain.model;

import java.util.List;

/**
 * Respuesta de una consulta de agregaci\u00f3n de MongoDB.
 * Contiene los documentos resultado y el c\u00f3digo JSON del pipeline empleado,
 * para que el frontend pueda mostrarlo/imprimirlo.
 */
public class MongoAggregateResponse {
    private List<Product> data;
    private String pipeline;

    public MongoAggregateResponse() {
    }

    public MongoAggregateResponse(List<Product> data, String pipeline) {
        this.data = data;
        this.pipeline = pipeline;
    }

    public List<Product> getData() {
        return data;
    }

    public void setData(List<Product> data) {
        this.data = data;
    }

    public String getPipeline() {
        return pipeline;
    }

    public void setPipeline(String pipeline) {
        this.pipeline = pipeline;
    }
}
