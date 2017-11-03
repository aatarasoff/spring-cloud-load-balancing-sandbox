package info.developerblog.spring.cloud.marathon.sample;

import com.netflix.loadbalancer.IPing;
import com.netflix.loadbalancer.PingUrl;
import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PingConfiguration {
    @Bean
    public IPing ping() {
        PingUrl pingUrl = new PingUrl();
        pingUrl.setPingAppendString("/health");
        return pingUrl;
    }

    @Bean public Retryer retryer() { return Retryer.NEVER_RETRY; }
}
