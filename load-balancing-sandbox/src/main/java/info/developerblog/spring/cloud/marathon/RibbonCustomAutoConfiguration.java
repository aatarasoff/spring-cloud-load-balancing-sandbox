package info.developerblog.spring.cloud.marathon;

import info.developerblog.spring.cloud.marathon.discovery.ribbon.RibbonMarathonAutoConfiguration;
import info.developerblog.spring.oneserver.ribbon.OneRibbonAutoConfiguration;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.cloud.netflix.ribbon.RibbonClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureAfter({ RibbonMarathonAutoConfiguration.class, OneRibbonAutoConfiguration.class })
@RibbonClients(value = {
        @RibbonClient(name = "cool-app", configuration = RibbonCustomConfiguration.class)
})
public class RibbonCustomAutoConfiguration {
}
