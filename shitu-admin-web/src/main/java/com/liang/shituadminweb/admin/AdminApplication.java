package com.liang.shituadminweb.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(AdminApplication.class, args);
        System.out.println("==========================================");
        System.out.println("   识途 管理后台启动成功！");
        System.out.println("   访问地址：http://localhost:8081");
        System.out.println("==========================================");
    }
}
