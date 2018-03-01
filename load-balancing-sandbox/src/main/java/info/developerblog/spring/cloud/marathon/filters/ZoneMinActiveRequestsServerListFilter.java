package info.developerblog.spring.cloud.marathon.filters;

import java.util.ArrayList;
import java.util.List;

import com.netflix.client.config.IClientConfig;
import com.netflix.config.ConfigurationManager;
import com.netflix.config.DeploymentContext;
import com.netflix.loadbalancer.LoadBalancerStats;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerStats;
import com.netflix.loadbalancer.ZoneAffinityServerListFilter;

/**
 * @author alexander.tarasov
 */
public class ZoneMinActiveRequestsServerListFilter extends ZoneAffinityServerListFilter<Server> {
    private String zone;

    @Override
    public void initWithNiwsConfig(IClientConfig niwsClientConfig) {
        super.initWithNiwsConfig(niwsClientConfig);
        if (ConfigurationManager.getDeploymentContext() != null) {
            this.zone = ConfigurationManager.getDeploymentContext().getValue(
                    DeploymentContext.ContextKey.zone);
        }
    }

    @Override
    public List<Server> getFilteredListOfServers(List<Server> servers) {
        List<Server> serverList = super.getFilteredListOfServers(servers);
        int minimalConcurrentConnections = Integer.MAX_VALUE;
        long currentTime = System.currentTimeMillis();
        List<Server> filtered = new ArrayList<>();
        for (Server server: serverList) {
            ServerStats serverStats = getLoadBalancerStats().getSingleServerStat(server);
            if (!serverStats.isCircuitBreakerTripped(currentTime)) {
                int concurrentConnections = serverStats.getActiveRequestsCount(currentTime);

                if (concurrentConnections < minimalConcurrentConnections) {
                    minimalConcurrentConnections = concurrentConnections;
                    filtered.clear();
                    filtered.add(server);
                }

                if (concurrentConnections == minimalConcurrentConnections) {
                    filtered.add(server);
                }
            }
        }

        if (filtered.isEmpty()) {
            return serverList;
        }

        return filtered;
    }
}
