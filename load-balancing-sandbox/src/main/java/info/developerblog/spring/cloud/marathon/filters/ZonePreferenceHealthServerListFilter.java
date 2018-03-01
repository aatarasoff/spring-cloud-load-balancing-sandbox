package info.developerblog.spring.cloud.marathon.filters;

import com.netflix.client.config.IClientConfig;
import com.netflix.config.ConfigurationManager;
import com.netflix.config.DeploymentContext;
import com.netflix.loadbalancer.LoadBalancerStats;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ZoneAffinityServerListFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author alexander.tarasov
 */
public class ZonePreferenceHealthServerListFilter extends ZoneAffinityServerListFilter<Server> {
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
        List<Server> output = super.getFilteredListOfServers(servers);
        if (this.zone != null && output.size() == servers.size()) {
            LoadBalancerStats stats = getLoadBalancerStats();

            List<Server> local = new ArrayList<>();
            for (Server server : output) {
                if (this.zone.equalsIgnoreCase(server.getZone())) {
                    if (stats != null) {
                        if (stats.getSingleServerStat(server).getActiveRequestsCount() < 10) {
                            local.add(server);
                        }
                    } else {
                        local.add(server);
                    }
                }
            }
            if (!local.isEmpty()) {
                return local;
            }
        }
        return output;
    }
}
