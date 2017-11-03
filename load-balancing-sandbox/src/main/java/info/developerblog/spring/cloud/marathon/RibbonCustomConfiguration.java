package info.developerblog.spring.cloud.marathon;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.IPing;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.RoundRobinRule;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerList;
import com.netflix.loadbalancer.ServerListFilter;
import com.netflix.loadbalancer.ServerListUpdater;
import com.netflix.loadbalancer.ZoneAwareLoadBalancer;
import info.developerblog.spring.cloud.marathon.filters.ZonePreferenceHealthServerListFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ComponentScan
public class RibbonCustomConfiguration {
    private IClientConfig clientConfig;
    private HystrixLogger hystrixLogger;

    @Autowired
    public RibbonCustomConfiguration(IClientConfig clientConfig,
                                     HystrixLogger hystrixLogger) {
        this.clientConfig = clientConfig;
        this.hystrixLogger = hystrixLogger;
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
        return new BalancerStatsProxy(config, rule, ping, serverList,
                serverListFilter, serverListUpdater);
    }

    public class BalancerStatsProxy extends ZoneAwareLoadBalancer {

        public BalancerStatsProxy(IClientConfig clientConfig, IRule rule,
                                  IPing ping, ServerList<Server> serverList, ServerListFilter<Server> filter,
                                  ServerListUpdater serverListUpdater) {
            super(clientConfig, rule, ping, serverList, filter, serverListUpdater);
        }

        @Override
        public Server chooseServer(Object key) {
            Server choosed = super.chooseServer(key);

            if (choosed != null) {
                log.debug("choosed: " + choosed.getHostPort());

                if (choosed.getHost().contains("zone1")) {
                    hystrixLogger.log1();
                }

                if (choosed.getHost().contains("zone2")) {
                    hystrixLogger.log2();
                }
            } else {
                hystrixLogger.logNull();
            }

            return choosed;
        }

    }
}
