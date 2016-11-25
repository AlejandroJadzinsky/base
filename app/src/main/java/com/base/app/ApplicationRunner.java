package com.base.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;

/** Sample Application runner.
 */
public class ApplicationRunner {
  /** The application logger. */
  private static final Logger LOG = LoggerFactory.getLogger(
      ApplicationRunner.class);

  /** Run the main application.
   *
   * @param args the arguments to run the application.
   */
  public void run(final String[] args) {
    LOG.trace("Launching application");
    SpringApplication runner = new SpringApplication(ApplicationRunner.class);
    runner.run(args);

    LOG.trace("Application launched");
  }
}
