package info.developerblog.spring.cloud.marathon.sample;

import java.net.SocketException;

import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.netflix.client.DefaultLoadBalancerRetryHandler;
import com.netflix.client.RetryHandler;
import com.netflix.client.config.IClientConfig;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import com.netflix.loadbalancer.IPing;
import com.netflix.loadbalancer.PingUrl;
import feign.Retryer;
import info.developerblog.spring.oneserver.client.OneHttpRequest;
import info.developerblog.spring.oneserver.client.OneHttpResponse;
import info.developerblog.spring.oneserver.ribbon.OneLoadBalancedHttpClient;
import info.developerblog.spring.oneserver.ribbon.OneLoadBalancer;
import info.developerblog.spring.oneserver.ribbon.OneLoadBalancerFactory;

@Configuration
public class AppConfiguration {
    @Bean
    public Retryer retryer() {
        return Retryer.NEVER_RETRY;
    }

    @Bean
    public OneLoadBalancerFactory oneLoadBalancerFactory(final SpringClientFactory springClientFactory) {
        return new OneLoadBalancerFactory(springClientFactory) {
            @Override
            protected OneLoadBalancedHttpClient createLoadBalancedHttpClient(IClientConfig config) {
                return new OneLoadBalancedHttpClient(config) {

                    public OneHttpResponse executeInternal(OneHttpRequest request) {
                        return super.execute(request);
                    }

                    @Override
                    public OneHttpResponse execute(OneHttpRequest request) {
                        String host = request.getUri().getHost();
                        if ("cool-app".equals(host)) {
                            host = "none";
                        }

                        int port = request.getUri().getPort();

                        return new HystrixCommand<OneHttpResponse>(
                                HystrixCommand.Setter
                                        .withGroupKey(HystrixCommandGroupKey.Factory.asKey("cool-app-cloud"))
                                        .andCommandKey(HystrixCommandKey.Factory.asKey("cool-app-" + host + (port > 0 ? "-" + port : "")))
                                        .andCommandPropertiesDefaults(
                                                HystrixCommandProperties.Setter()
                                                        .withCircuitBreakerEnabled(false)
//                                                        .withCircuitBreakerErrorThresholdPercentage(100)
                                                        .withFallbackEnabled(false)
                                                        .withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.THREAD)
                                                        .withExecutionTimeoutEnabled(true)
                                                        .withExecutionTimeoutInMilliseconds(4900)
                                        )
                                        .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey("cool-app-pool-cloud"))
                                        .andThreadPoolPropertiesDefaults(
                                                HystrixThreadPoolProperties.Setter()
                                                        .withCoreSize(500)
                                                        .withMaximumSize(500)
                                        )
                        ) {
                            @Override
                            protected OneHttpResponse run() throws Exception {
                                OneHttpResponse response = executeInternal(request);

                                if (!response.isSuccess()) {
                                    throw new SocketException("no success");
                                }

                                return response;
                            }
                        }.execute();
                    }
                };
            }

            @Override
            protected void modifyLoadBalancer(OneLoadBalancer client) {
                super.modifyLoadBalancer(client);
                client.setRetryHandler(new AllErrorsTrippedRetryHandler());
            }
        };
    }

    public static class AllErrorsTrippedRetryHandler extends DefaultLoadBalancerRetryHandler {
        @Override
        public boolean isCircuitTrippingException(Throwable e) {
            return e != null;
        }
    }
}
