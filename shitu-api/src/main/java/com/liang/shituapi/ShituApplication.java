package com.liang.shituapi;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.liang.shituapi.dao")
public class ShituApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShituApplication.class, args);
        System.out.println("==========================================");
        System.out.println("   识途 API 启动成功！");
        System.out.println("   访问地址：http://localhost:8080");
        System.out.println("==========================================");
    }
}
