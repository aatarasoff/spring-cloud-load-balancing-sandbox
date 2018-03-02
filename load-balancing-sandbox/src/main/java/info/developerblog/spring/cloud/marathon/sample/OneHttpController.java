package info.developerblog.spring.cloud.marathon.sample;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMethod;

import com.netflix.client.ClientException;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import info.developerblog.spring.oneserver.client.OneHttpRequest;
import info.developerblog.spring.oneserver.client.OneHttpResponse;
import info.developerblog.spring.oneserver.ribbon.OneLoadBalancerFactory;
import info.developerblog.spring.oneserver.server.HttpController;
import lombok.extern.slf4j.Slf4j;

import one.nio.http.Path;
import one.nio.http.Response;

/**
 * @author alexander.tarasov
 */
@Slf4j
@HttpController
public class OneHttpController {
    @Autowired
    private OneLoadBalancerFactory oneLoadBalancerFactory;

    @Value("${cool.application.name:cool-app}")
    private String serviceId;

    @Path("/callme")
    public Response callme() throws ClientException {
        return new HystrixCommand<Response>(
                HystrixCommand.Setter
                        .withGroupKey(HystrixCommandGroupKey.Factory.asKey("cool-app-all"))
                        .andCommandKey(HystrixCommandKey.Factory.asKey("cool-app-call"))
                        .andCommandPropertiesDefaults(
                                HystrixCommandProperties.Setter()
                                    .withCircuitBreakerEnabled(true)
                                    .withCircuitBreakerErrorThresholdPercentage(50)
                                    .withFallbackEnabled(true)
                                    .withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.THREAD)
                                    .withExecutionTimeoutEnabled(true)
                                    .withExecutionTimeoutInMilliseconds(10000)
                        )
                        .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey("cool-app-pool-all"))
                        .andThreadPoolPropertiesDefaults(
                                HystrixThreadPoolProperties.Setter()
                                    .withCoreSize(500)
                                    .withMaximumSize(500)
                        )
        ) {
            @Override
            protected Response run() throws Exception {
                OneHttpResponse response = oneLoadBalancerFactory
                        .create("cool-app")
                        .executeWithLoadBalancer(
                                new OneHttpRequest(RequestMethod.GET, "http://"+ serviceId +"/me")
                        );

                if (response.isSuccess()) {
                    return Response.ok(new String((byte[]) response.getPayload(), StandardCharsets.UTF_8));
                }

                log.error("Error while calling");

                throw new Exception("no success");
            }

            @Override
            protected Response getFallback() {
                return new Response(Response.INTERNAL_ERROR);
            }
        }.execute();
    }
}
