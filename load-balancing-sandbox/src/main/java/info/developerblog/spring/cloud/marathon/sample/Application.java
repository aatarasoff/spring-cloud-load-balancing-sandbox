package info.developerblog.spring.cloud.marathon.sample;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
import org.springframework.cloud.netflix.metrics.MetricsClientHttpRequestInterceptor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Configuration
@EnableAutoConfiguration
@EnableConfigurationProperties
@RestController
@EnableFeignClients
@EnableDiscoveryClient
@EnableHystrix
@EnableHystrixDashboard
@SpringBootApplication
public class Application {
    @Autowired
    private LoadBalancerClient loadBalancer;

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private SampleClient sampleClient;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${cool.application.name:cool-app}")
    private String serviceId;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @RequestMapping("/me")
    public String me() throws UnknownHostException {
        return serviceId + " @ " + InetAddress.getLocalHost().getHostName();
    }

    @RequestMapping("/services")
    public List<String> services() {
        return discoveryClient.getServices();
    }

    @RequestMapping("/instances")
    public List<ServiceInstance> instances() {
        return discoveryClient.getInstances(serviceId);
    }

    @RequestMapping("/")
    public ServiceInstance lb() {
        return loadBalancer.choose(serviceId);
    }

    @RequestMapping("/url")
    public String realUrl() throws IOException {
        return loadBalancer.execute(serviceId, instance ->
                loadBalancer.reconstructURI(
                        instance,
                        new URI("http://"+ serviceId +"/me")
                )
        ).toString();
    }

    @RequestMapping("/choose")
    public String choose() {
        return loadBalancer.choose(serviceId).getUri().toString();
    }

    @RequestMapping("/rest")
    public String rest() {
        return this.restTemplate.getForObject("http://"+ serviceId +"/me", String.class);
    }

    @RequestMapping("/feign")
    public String feign() {
        return sampleClient.call();
    }

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @FeignClient("cool-app")
    public interface SampleClient {
        @RequestMapping(value = "/me", method = RequestMethod.GET)
        String call();
    }
}
