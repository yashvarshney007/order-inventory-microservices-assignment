 package com.yash.order.config;

 import io.swagger.v3.oas.models.OpenAPI;

 import io.swagger.v3.oas.models.info.Info;

 import io.swagger.v3.oas.models.info.Contact;

 import org.springframework.context.annotation.Bean;

 import org.springframework.context.annotation.Configuration;

 /** 

  * OpenAPI/Swagger configuration for Order Service 

  */

 @Configuration

 public class OpenApiConfig {

   @Bean

   public OpenAPI orderServiceOpenAPI() {

     return new OpenAPI()

       .info(new Info()

         .title("Order Service API")

         .description("Microservice for processing customer orders. " +

           "Communicates with Inventory Service to validate stock availability and update inventory. " +

           "Implements transaction management and error handling.")

         .version("1.0.0")

         .contact(new Contact()

           .name("Yash")

           .email("yashvarshney492@gmail.com")));

   }

 }