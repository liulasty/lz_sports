package com.lz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
@MapperScan("com.lz.mapper")
public class BackendApplication {
    private static final Logger log = LoggerFactory.getLogger(BackendApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
        //http://localhost:8080/swagger-ui/index.html 或 http://localhost:8080/doc.html (取决于具体集成方式，当前配置支持标准 Swagger UI)。
        log.info("Backend Application Started!");
        log.info("Swagger UI: http://localhost:8080/api/swagger-ui/index.html");
        log.info("http://localhost:8080/api/doc.html#/home");
    }
}
