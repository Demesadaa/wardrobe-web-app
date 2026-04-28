package com.wardrobe;

import com.wardrobe.wardrobe.StorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class WardrobeApplication {
    public static void main(String[] args) {
        SpringApplication.run(WardrobeApplication.class, args);
    }
}
