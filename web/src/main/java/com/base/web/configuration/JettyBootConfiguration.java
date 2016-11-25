package com.base.web.configuration;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.jetty
    .JettyEmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.jetty.JettyServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** This class provides an entry point to configure Jetty when used with
 * Spring Boot.
 * @author diego.
 */
@Configuration
public class JettyBootConfiguration {

  /** Retrieves the Jetty Factory. This factory can be used to configure the
   * Jetty Server.
   * @param port the port to initialize the server.
   * @param path the servlet context path.
   * @param serverCustomizer the server customizer.
   * @return the factory.
   */
  @Bean
  public JettyEmbeddedServletContainerFactory
    jettyEmbeddedServletContainerFactory(
      @Value("${server.port:8080}")
      final String port,
      @Value("${server.contextPath://*}")
      final String path,
      final JettyServerCustomizer serverCustomizer) {
    final JettyEmbeddedServletContainerFactory factory =
        new JettyEmbeddedServletContainerFactory(path, Integer.valueOf(port));
    factory.addServerCustomizers(serverCustomizer);
    return factory;
  }

  /** Jetty server customizer.
   *
   * @param minThreads the minimum number of active threads.
   * @param maxThreads the maximum number of active threads.
   * @param idleTimeOut Set the maximum thread idle time. Threads that are idle
   * for longer than this period may be stopped. Delegated to the named or
   * anonymous Pool.
   *
   * @return a JettyServerCustomizer instance, never null.
   */
  @Bean
  public JettyServerCustomizer serverCustomizer(
      @Value("${jetty.minThreads:50}") final String minThreads,
      @Value("${jetty.maxThreads:300}") final String maxThreads,
      @Value("${jetty.idleTime:60000}") final String idleTimeOut) {
    JettyServerCustomizer customizer = new JettyServerCustomizer() {
      @Override
      public void customize(final Server pServer) {
        QueuedThreadPool pool = pServer.getBean(QueuedThreadPool.class);
        pool.setMinThreads(Integer.valueOf(minThreads));
        pool.setMaxThreads(Integer.valueOf(maxThreads));
        pool.setIdleTimeout(Integer.valueOf(idleTimeOut));
      }
    };
    return customizer;
  }
}
