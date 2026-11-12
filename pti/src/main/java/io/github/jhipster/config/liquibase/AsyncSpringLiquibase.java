package io.github.jhipster.config.liquibase;

import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.task.TaskExecutor;
import org.springframework.util.StopWatch;

public class AsyncSpringLiquibase extends SpringLiquibase {

    private final Logger log = LoggerFactory.getLogger(AsyncSpringLiquibase.class);
    private final TaskExecutor taskExecutor;
    private final Environment env;

    public AsyncSpringLiquibase(TaskExecutor taskExecutor, Environment env) {
        this.taskExecutor = taskExecutor;
        this.env = env;
    }

    @Override
    public void afterPropertiesSet() throws LiquibaseException {
        taskExecutor.execute(() -> {
            try {
                log.debug("Starting Liquibase asynchronously");
                StopWatch watch = new StopWatch();
                watch.start();
                super.afterPropertiesSet();
                watch.stop();
                log.debug("Liquibase finished in {} ms", watch.getTotalTimeMillis());
            } catch (LiquibaseException e) {
                log.error("Liquibase could not start", e);
            }
        });
    }
}
