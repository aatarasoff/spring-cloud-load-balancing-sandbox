package info.developerblog.spring.cloud.marathon;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.DynamicServerListLoadBalancer;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.IPing;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.RoundRobinRule;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerList;
import com.netflix.loadbalancer.ServerListFilter;
import com.netflix.loadbalancer.ServerListUpdater;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class RibbonCustomConfiguration {
    private IClientConfig clientConfig;

    @Autowired
    public RibbonCustomConfiguration(IClientConfig clientConfig) {
        this.clientConfig = clientConfig;
    }

    @Bean
    public IRule ribbonRule() {
        RoundRobinRule rule = new RoundRobinRule();
        rule.initWithNiwsConfig(clientConfig);
        return rule;
    }

    @Bean
    public ServerListFilter<Server> ribbonServerListFilter() {
        return servers -> servers;
    }

    @Bean
    public ILoadBalancer ribbonLoadBalancer(IClientConfig config,
                                            ServerList<Server> serverList, ServerListFilter<Server> serverListFilter,
                                            IRule rule, IPing ping, ServerListUpdater serverListUpdater) {
        return new DynamicServerListLoadBalancer<>(config, rule, ping, serverList, serverListFilter, serverListUpdater);
    }
}
