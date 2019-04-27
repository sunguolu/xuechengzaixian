package com.xuecheng.manage_cms;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EntityScan("com.xuecheng.framework.domain.cms")//扫描实体类
@ComponentScan(basePackages={"com.xuecheng.api","com.xuecheng.manage_cms","com.xuecheng.framework"})//扫描接口和本地包
public class ManageCmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ManageCmsApplication.class);
    }
}
