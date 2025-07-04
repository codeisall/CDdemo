package com.pdsu.charge_palteform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.pdsu.charge_palteform.mapper")
@SpringBootApplication
public class ChargePalteformApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChargePalteformApplication.class, args);
    }

}
