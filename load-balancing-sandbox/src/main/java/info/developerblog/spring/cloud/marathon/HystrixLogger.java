package info.developerblog.spring.cloud.marathon;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.stereotype.Component;

@Component
public class HystrixLogger {
    @Autowired
    private CounterService counterService;

    @HystrixCommand(groupKey = "calls", commandKey = "call.instance1")
    public void log1() {
        counterService.increment("meter.call.server.instance1");
    }

    @HystrixCommand(groupKey = "calls", commandKey = "call.instance2")
    public void log2() {
        counterService.increment("meter.call.server.instance2");
    }

    @HystrixCommand(groupKey = "calls", commandKey = "call.null")
    public void logNull() {
        counterService.increment("meter.call.server.null");
    }
}
