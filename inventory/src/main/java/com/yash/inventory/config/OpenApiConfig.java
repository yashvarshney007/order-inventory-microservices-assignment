 

package com.yash.inventory.config; 

import io.swagger.v3.oas.models.OpenAPI; 

import io.swagger.v3.oas.models.info.Info; 

import io.swagger.v3.oas.models.info.Contact; 

import org.springframework.context.annotation.Bean; 

import org.springframework.context.annotation.Configuration; 

/** 

 * OpenAPI/Swagger configuration for Inventory Service 

 */ 
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI inventoryServiceOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Inventory Service API")
                .description("Microservice for managing product inventory with batch tracking and expiry management. "
                    + "Implements Factory Design Pattern for flexible inventory allocation strategies (FIFO, FEFO).")
                .version("1.0.0")
                .contact(new Contact()
                    .name("Yash")
                    .email("yashvarshney492@gmail.com")));
    }
}
