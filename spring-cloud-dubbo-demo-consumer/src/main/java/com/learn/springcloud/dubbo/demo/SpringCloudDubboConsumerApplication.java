package com.learn.springcloud.dubbo.demo;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.learn.dubbo.demo.DubboDemoService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.context.annotation.Bean;

/**
 *  spring cloud demo consumer
 * @author shunzhong.deng
 * @date 6/28/18 2:26 PM
 * @version 1.0
 **/
@SpringBootApplication
@EnableHystrix
@EnableDiscoveryClient
public class SpringCloudDubboConsumerApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringCloudDubboConsumerApplication.class, args);
    }

    @Bean
    public ApplicationConfig getApplication() {
        // 当前应用配置
        ApplicationConfig application = new ApplicationConfig();
        application.setName("spring-cloud-consumer-dubbo");
        return application;
    }

    @Bean
    public RegistryConfig getRegistry() {
        // 连接注册中心配置
        RegistryConfig registry = new RegistryConfig();
        registry.setAddress("10.0.4.79:2182");
        registry.setProtocol("zookeeper");
        // 注意：ReferenceConfig为重对象，内部封装了与注册中心的连接，以及与服务提供方的连接
        return registry;
    }

    @Bean
    public DubboDemoService getDubboDemoService() {
        // 引用远程服务
        // 此实例很重，封装了与注册中心的连接以及与提供者的连接，请自行缓存，否则可能造成内存和连接泄漏
        ReferenceConfig<DubboDemoService> reference = new ReferenceConfig<>();
        reference.setApplication(getApplication());
        // 多个注册中心可以用setRegistries()
        reference.setRegistry(getRegistry());
        reference.setInterface(DubboDemoService.class);
        // reference.setVersion("1.0.0");

        // 和本地bean一样使用xxxService,  注意：此代理对象内部封装了所有通讯细节，对象较重，请缓存复用
        DubboDemoService dubboDemoService = reference.get();
        return  dubboDemoService;
    }
}
