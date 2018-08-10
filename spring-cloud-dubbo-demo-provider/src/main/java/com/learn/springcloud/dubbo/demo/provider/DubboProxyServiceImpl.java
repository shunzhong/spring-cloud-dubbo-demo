package com.learn.springcloud.dubbo.demo.provider;

import com.alibaba.dubbo.config.annotation.Service;
import com.learn.springcloud.dubbo.demo.DubboProxyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;

import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;

/**
 * 对外提供 dubbo 协议，内部通过 springcloud 进行 调用
 *
 * @author shunzhong.deng
 * @version 1.0
 * @date 7/6/18 11:01 AM
 **/
@Service
public class DubboProxyServiceImpl implements DubboProxyService {

    @Autowired
    private LoadBalancerClient loadBalancerClient;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public String sayHelloCallSpringCloud(String name) throws IOException {
//        return "spring cloud -> dubbo, hello " + name;

        String result = "";
        try {
            result = callSpringCloudService("spring-cloud-provider-demo",
                    "/sayHello/88800", String.class, Void.class, HttpMethod.GET);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public String sayHello(String name) throws IOException {

        return "dubbo proxy called success";
    }

    /**
     * 统一封装springcloud服务的远程调用
     * @param serviceId 远程spring cloud 对应的服务应用名称 spring.application.name
     * @param uri 统一资源标识符，如 abc.com.cn/aaa,URI字段为 /aaa；
     * @param responseType 请求对应的响应类型
     * @param request 请求对象
     * @param httpMethod 对应的请求方法
     * @param <T> 响应 类型
     * @param <E> 请求参数类型
     * @return
     * @throws IOException
     */
    private <T, E> T callSpringCloudService(final String serviceId, final String uri, Class<T> responseType, E request,
                                                             HttpMethod httpMethod) throws IOException, RuntimeException {
        // http 分隔符
        final String HTTP_SEPARATOR = "/";
        if (StringUtils.isEmpty(serviceId) || StringUtils.isEmpty(uri)) {
            throw new RuntimeException("serviceId 或 uri 为空");
        }

        // 从注册中心上获取serviceId 对应的服务实例
        ServiceInstance serviceInstance = loadBalancerClient.choose(serviceId);
        return  loadBalancerClient.execute(serviceId, serviceInstance, instance -> {

            String uri2 = uri;
            if(!uri2.startsWith(HTTP_SEPARATOR)) {
                uri2 = HTTP_SEPARATOR + uri2;
            }
            // 组织请求地址
            String url = "http://" + instance.getHost() + ":" + instance.getPort() + uri2;
            HttpEntity<E> httpEntity = new HttpEntity<>(request, null);
            ResponseEntity<T> responseEntity = restTemplate.exchange(URI.create(url), httpMethod, httpEntity, responseType);
            if (responseEntity.getStatusCode().isError()) {
                throw new RuntimeException("客户端获取服务端错误，htt状态码：" + responseEntity.getStatusCode());
            }
            return  responseEntity.getBody();
        });

    }


}
