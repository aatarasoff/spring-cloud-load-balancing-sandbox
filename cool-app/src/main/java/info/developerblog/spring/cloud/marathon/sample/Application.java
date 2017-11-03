package info.developerblog.spring.cloud.marathon.sample;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.cloud.client.loadbalancer.LoadBalancerRequest;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.netflix.zuul.EnableZuulServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


@Slf4j
@Configuration
@EnableAutoConfiguration
@RestController
@EnableConfigurationProperties
@SpringBootApplication
public class Application {
    @Autowired
    private Environment environment;

    @Value("${spring.application.name:cool-app}")
    private String serviceId;

    private AtomicLong counter = new AtomicLong();

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @RequestMapping("/me")
    public String me() throws UnknownHostException, InterruptedException {
        long multiplicator = (counter.incrementAndGet() / 10) + 1;

        LocalDateTime now = LocalDateTime.now();
        int second = now.getSecond();

        long delay = Long.valueOf(environment.getProperty("TEST_APP_DELAY", "100"));

        String host = environment.getProperty("HOST", "unknownhost");
        if (host.equals("mesos-slave.zone1")) {
            if (second % 10 < 5) {
                delay = delay * 5;
            }
        }

        if (host.equals("mesos-slave.zone2")) {
            if (second % 10 >= 5) {
                delay = delay * 5;
            }
        }

        Thread.sleep(delay * multiplicator);

        String response = serviceId + " @ " + host
                + ":" + environment.getProperty("PORT0", "9090")
                + " with docker hostname: " + InetAddress.getLocalHost().getHostName();

        counter.decrementAndGet();

        return response;
    }
}
