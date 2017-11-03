package info.developerblog.spring.cloud.marathon.filters;

import com.netflix.loadbalancer.AbstractServerListFilter;
import com.netflix.loadbalancer.LoadBalancerStats;
import com.netflix.loadbalancer.Server;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author alexander.tarasov
 */
public class CircuirBreakerTrippedServerListFilter extends AbstractServerListFilter<Server> {
    @Override
    public List<Server> getFilteredListOfServers(List<Server> servers) {
        LoadBalancerStats stats = getLoadBalancerStats();
        if (stats == null) {
            return servers;
        }
        return servers.stream()
                .filter(server -> !stats.isCircuitBreakerTripped(server))
                .collect(Collectors.toList());
    }
}
