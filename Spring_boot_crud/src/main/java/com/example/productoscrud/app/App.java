package com.example.productoscrud.app;

import com.example.productoscrud.application.ports.ProductRepository;
import com.example.productoscrud.application.service.ProductMongoService;
import com.example.productoscrud.application.service.ProductService;
import com.example.productoscrud.infrastructure.persistence.mongo.ProductRepositoryMongo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(scanBasePackages = "com.example.productoscrud")
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    // Servicio para la persistencia SQL (MySQL)
    @Bean
    public ProductService productService(@Qualifier("mysqlRepository") ProductRepository productRepository) {
        return new ProductService(productRepository);
    }

    // Servicio para la persistencia NoSQL (MongoDB)
    @Bean
    public ProductMongoService productMongoService(ProductRepositoryMongo productRepositoryMongo) {
        return new ProductMongoService(productRepositoryMongo);
    }
}

// Origen: la JVM llama a App.main() (com.example.productoscrud.app).
// Destino: App.main() inicia Spring (org.springframework.boot); Spring llama a App.productService()
// y conecta ProductService (application.service) con ProductRepository (application.ports).
