package com.learn.springcloud.dubbo.demo;



import java.io.IOException;

/**
 * 对外提供dubbo服务，服务提供方 provider
 * @author shunzhong.deng
 * @version 1.0
 * @date 7/6/18 11:00 AM
 **/
public interface DubboProxyService {

    String sayHello(String name) throws IOException;

    String sayHelloCallSpringCloud(String name) throws IOException;

}
