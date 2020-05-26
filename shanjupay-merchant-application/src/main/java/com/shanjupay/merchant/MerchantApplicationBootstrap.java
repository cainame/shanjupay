package com.shanjupay.merchant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author Administrator
 * @version 1.0
 **/
@SpringBootApplication
@EnableDiscoveryClient
public class MerchantApplicationBootstrap {
    public static void main(String[] args) {
        SpringApplication.run(MerchantApplicationBootstrap.class,args);
    }

    @Bean
    RestTemplate restTemplate(){
        //使用OkHttpClient实例化RestTemplate
        RestTemplate restTemplate = new RestTemplate(new OkHttp3ClientHttpRequestFactory());
        //当RestTemplate默认使用String存储body内容时默认使用ISO_8859_1字符集,得到消息转换器
        List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters();
        //指定StringHttpMessageConverter消息转换器的字符集为utf-8
        messageConverters.set(1,new StringHttpMessageConverter(StandardCharsets.UTF_8));
        return  restTemplate;
    }
}
