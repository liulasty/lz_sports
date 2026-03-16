package com.lz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
@MapperScan("com.lz.mapper")
@EnableAsync
@EnableScheduling
public class BackendApplication {
    private static final Logger log = LoggerFactory.getLogger(BackendApplication.class);

    public static void main(String[] args) {
        // Load .env file
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        // If JWT_SECRET is missing, try loading from subdirectory (useful when running from project root instead of module root)
        if (dotenv.get("JWT_SECRET") == null) {
            Dotenv projectDotenv = Dotenv.configure()
                    .directory("./lz_sports_backend")
                    .ignoreIfMissing()
                    .load();
            if (projectDotenv.get("JWT_SECRET") != null) {
                dotenv = projectDotenv;
            }
        }
        
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));

        SpringApplication.run(BackendApplication.class, args);
        //http://localhost:8080/swagger-ui/index.html 或 http://localhost:8080/doc.html (取决于具体集成方式，当前配置支持标准 Swagger UI)。
        log.info("Backend Application Started!");
        log.info("Swagger UI: http://localhost:8080/swagger-ui/index.html");
        log.info("http://localhost:8080/doc.html#/home");
    }
}
