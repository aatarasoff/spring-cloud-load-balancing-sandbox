package info.developerblog.spring.cloud.marathon.filters;

import com.netflix.loadbalancer.AbstractServerListFilter;
import com.netflix.loadbalancer.LoadBalancerStats;
import com.netflix.loadbalancer.Server;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author alexander.tarasov
 */
public class CircuitBreakerTrippedServerListFilter extends AbstractServerListFilter<Server> {
    @Override
    public List<Server> getFilteredListOfServers(List<Server> servers) {
        LoadBalancerStats stats = getLoadBalancerStats();
        if (stats == null) {
            return servers;
        }
        return servers.stream()
                .filter(server -> !stats.getSingleServerStat(server).isCircuitBreakerTripped())
//                .filter(server -> stats.getSingleServerStat(server).getResponseTime99point5thPercentile() < 4000.0d)
                .collect(Collectors.toList());
    }
}
