package info.developerblog.spring.cloud.marathon.sample;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMethod;

import com.netflix.client.ClientException;
import info.developerblog.spring.oneserver.client.OneHttpRequest;
import info.developerblog.spring.oneserver.client.OneHttpResponse;
import info.developerblog.spring.oneserver.ribbon.OneLoadBalancerFactory;
import info.developerblog.spring.oneserver.server.HttpController;

import one.nio.http.Param;
import one.nio.http.Path;
import one.nio.http.Response;

/**
 * @author alexander.tarasov
 */
@HttpController
public class ItsMeController {
    @Autowired
    private Environment environment;

    @Value("${spring.application.name:cool-app}")
    private String serviceId;

    private AtomicLong counter = new AtomicLong();

    @Path("/me")
    public Response me() throws InterruptedException, UnknownHostException {
        long multiplier = (counter.incrementAndGet() / 10) + 1;

        long delay = Long.valueOf(environment.getProperty("TEST_APP_DELAY", "100"));

        LocalDateTime now = LocalDateTime.now();
        int second = now.getSecond();

        String host = environment.getProperty("HOST", "unknownhost");
        if (host.equals("mesos-slave.zone1")) {
            if (second % 10 < 5) {
                delay = delay * 2;
            }
        }

        if (host.equals("mesos-slave.zone2")) {
            delay = delay * 3;

            if (second % 10 >= 5) {
                delay = delay * 2;
            }
        }

        Thread.sleep(Math.min(delay * multiplier, 5000L));

        String response = serviceId + " @ " + host
                + ":" + environment.getProperty("PORT0", "9090")
                + " with docker hostname: " + InetAddress.getLocalHost().getHostName();

        counter.decrementAndGet();

        return Response.ok(response);
    }

    @Path("/health")
    public Response health() {
        return Response.ok("");
    }

    @Path("/delayUp")
    public Response delay(@Param("delay") String delay) {
        ((ConfigurableEnvironment) environment).getSystemProperties().put("TEST_APP_DELAY", delay);
        return Response.ok("");
    }
}
