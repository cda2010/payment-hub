

package com.m2i.pti;

import com.m2i.pti.config.ApplicationProperties;
import com.m2i.pti.config.DefaultProfileUtil;

import com.m2i.pti.scheduler.service.SchedulerService;
import io.github.jhipster.config.JHipsterConstants;
import io.github.jhipster.config.JHipsterProperties;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import jakarta.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cache.annotation.EnableCaching;


@ComponentScan
@EnableCaching
@EnableAutoConfiguration(exclude = {KafkaAutoConfiguration.class})
@EnableConfigurationProperties({LiquibaseProperties.class, ApplicationProperties.class, JHipsterProperties.class})
public class PtiApp implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(PtiApp.class);

    
    @Value("{spring.application.master}")
    private String master;
    private final Environment env;
       @Autowired  
SchedulerService schedulerService;
    public PtiApp(Environment env) {
        this.env = env;
    }
@PreDestroy
    public void onDestroy() throws Exception {
      //  schedulerService.stopSchedulers();
        System.out.println("Spring Container is destroyed!");
    }
    /**
     * Initializes pti.
     * <p>
     * Spring profiles can be configured with a program arguments --spring.profiles.active=your-active-profile
     * <p>
     * You can find more information on how profiles work with JHipster on <a href="https://jhipster.github.io/profiles/">https://jhipster.github.io/profiles/</a>.
     */
    @PostConstruct
    public void initApplication() {
        Collection<String> activeProfiles = Arrays.asList(env.getActiveProfiles());
        if (activeProfiles.contains(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT) && activeProfiles.contains(JHipsterConstants.SPRING_PROFILE_PRODUCTION)) {
            log.error("You have misconfigured your application! It should not run " +
                    "with both the 'dev' and 'prod' profiles at the same time.");
        }
        if (activeProfiles.contains(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT) && activeProfiles.contains(JHipsterConstants.SPRING_PROFILE_CLOUD)) {
            log.error("You have misconfigured your application! It should not " +
                    "run with both the 'dev' and 'cloud' profiles at the same time.");
        }
    }
  
    /**
     * Main method, used to run the application.
     *
     * @param args the command line arguments
     * @throws UnknownHostException if the local host name could not be resolved into an address
     */
    public    static void main(String[] args) throws UnknownHostException, InterruptedException, SchedulerException {
        System.setProperty("jasypt.encryptor.password", "password");
        SpringApplication app = new SpringApplication(PtiApp.class);

        DefaultProfileUtil.addDefaultProfile(app);
        Environment env = app.run(args).getEnvironment();
        String protocol = "http";
        if (env.getProperty("server.ssl.key-store") != null) {
            protocol = "https";
        }
        log.info("\n----------------------------------------------------------\n\t" +
                        "Application '{}' is running! Access URLs:\n\t" +
                        "Local: \t\t{}://localhost:{}\n\t" +
                        "External: \t{}://{}:{}\n\t" +
                        "Profile(s): \t{}\n----------------------------------------------------------",
                env.getProperty("spring.application.name"),
                protocol,
                env.getProperty("server.port"),
                protocol,
                InetAddress.getLocalHost().getHostAddress(),
                env.getProperty("server.port"),
                env.getActiveProfiles());
     //   PtiApp quartzSchedulerExample = new PtiApp(env);
      //  quartzSchedulerExample.fireJob();
    }
      @Override
    public void run(String... args) throws Exception {
        if (master.equals("1")){
              schedulerService.startAllSchedulers();
        }
       
       // schedulerService.startAllSchedulers();
    }
    

}
