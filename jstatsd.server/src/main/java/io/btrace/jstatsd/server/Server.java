package io.btrace.jstatsd.server;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.btrace.jstatsd.server.api.Config;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.IOException;
import java.util.Properties;

/**
 * Main entry point to jStatsD server
 */
public class Server {
    public static void main(String[] args) throws IOException {
        OptionParser op = new OptionParser("c:p:s");
        OptionSet options = op.parse(args);

        final Properties props = new Properties();

//        props.put(Config.QUEUE_WORKERS_CAP_KEY, "1");
//        props.put(Config.RECEIVER_ASYNC_KEY, "false");
        props.put(Config.AGGREGATOR_SINKS_KEY, "console,elastic-search");

        if (options.has("p")) {
            if (options.hasArgument("p")) {
                props.put(Config.RECEIVER_PORT_KEY, options.valueOf("p"));
            }
        }

        if (options.has("s")) {
            props.put(Config.RECEIVER_ASYNC_KEY, "false");
        }


        final ServerInstance s = instance(props);

        System.out.println("=== Starting jStatsD server");
        s.start();

        System.out.println("=== Started.");
        System.out.println("=== Press any key to stop the server");

        System.in.read();

        s.stop();
    }

    /**
     * Creates a new {@linkplain ServerInstance} object configured with the provided properties.
     * @param props the configuration properties - keys are from {@linkplain Config} class
     * @return a new configured {@linkplain ServerInstance} object
     */
    public static ServerInstance instance(Properties props) {
        Injector injector = Guice.createInjector(
            new MainModule(props)
        );

        return new ServerInstance(injector.getInstance(ServerImpl.class));
    }
}