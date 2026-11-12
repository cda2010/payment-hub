package com.m2i.pti.batch.reader.validation;

import com.m2i.pti.batch.reader.LigneChargementTransDTO;
import com.m2i.pti.domain.Operation;
import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.validator.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@EnableBatchProcessing

public class BatchChargementTransfertConfig {

    @Autowired
    StepBuilderFactory stepBuilderFactory;
    @Autowired
    JobBuilderFactory JobBuilderFactory;
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    PlatformTransactionManager transactionManager;
    //  @Bean
    // @StepScope
//    public ItemReader<LigneChargementTransDTO> databaseCursorItemReader(DataSource dataSource,
//            @Value("#{jobParameters['codeoperation']}") Long codeoperation,
//            @Value("#{jobParameters['compteId']}") Long compteId,
//            @Value("#{jobParameters['chargementId']}") Long idChargement
//    ) {
//        String sql = "SELECT id as idLigne, " + codeoperation + " as codeoperation ," + compteId + " as compteId FROM ligne_chargement_transfert  where chargement_transfert_id= " + idChargement + " and statut_ligne!='OC' and compte_id is not null order by id";
//        return new JdbcCursorItemReaderBuilder<LigneChargementTransDTO>()
//                .name("cursorItemReader")
//                .dataSource(jdbcTemplate.getDataSource())
//                .sql(sql)
//                .rowMapper(new BeanPropertyRowMapper<>(LigneChargementTransDTO.class))
//                .build();
//    }

    public class DataErrorSkipper implements SkipPolicy {

        @Override
        public boolean shouldSkip(Throwable exception, long skipCount) throws SkipLimitExceededException {
            if (exception instanceof Exception) {
                return true;
            }
            return true;
        }
    }

    @Bean
    Step getStepTrans(@Qualifier("ligneReader") ItemReader<LigneChargementTransDTO> transfertItemReader, @Qualifier("ligneWriter") ItemWriter<LigneChargementTransDTO> writer) {
        Step step = stepBuilderFactory.get("valide-chargement-fichier-batch")
                .<LigneChargementTransDTO, LigneChargementTransDTO>chunk(1, transactionManager)
                .reader(transfertItemReader)
                .processor(validationLigneChargementTransfertProcessor())
                .writer(writer).faultTolerant().
                skipPolicy(new DataErrorSkipper())
                .listener(skipListener())
                .noRollback(Exception.class)
                .noRetry(Exception.class)
                .processorNonTransactional()
                //.taskExecutor(taskExecutor())
                //           .listener(steplistener())
                .build();
        return step;
    }

    @Bean
    @Qualifier(value = "ligneTransValidationJob")
    Job ligneTransValidationJob(@Qualifier("ligneReader") ItemReader<LigneChargementTransDTO> transfertItemReader, @Qualifier("ligneWriter") ItemWriter<LigneChargementTransDTO> writer) {
        Step stepSlave = getStepTrans(transfertItemReader, writer);
        Job job = JobBuilderFactory.get("validjobchargement")
                .incrementer(new RunIdIncrementer())
                .start(stepSlave)
                .listener(jobChargementlistener())
                .build();
        return job;
    }

    @Bean
    public SkipListener skipListener() {
        return new StepSkipListener();
    }

    @Bean
    @StepScope
    public ItemReader<LigneChargementTransDTO> ligneReader(@Value("#{jobParameters['codeoperation']}") Long codeoperation,
            @Value("#{jobParameters['compteId']}") Long compteId,
            @Value("#{jobParameters['chargementId']}") Long idChargement) {

        System.out.println("Chargement ID==================" + idChargement);
        return new InMemoryLigneChargementTransfertReader1();
    }

    @Bean
    @StepScope
    public ItemWriter<LigneChargementTransDTO> ligneWriter() {
        return new ValidationLigneChargementTransfertWriter();
    }

    @Bean
    @StepScope
    public ChargementTransfertProcessor validationLigneChargementTransfertProcessor() {
        return new ChargementTransfertProcessor();
    }

    @Bean
    public JobInterfaceListener jobChargementlistener() {
        return new JobInterfaceListener();
    }

    @Bean(name = "asyncJobLauncher")
    public JobLauncher simpleJobLauncher(JobRepository jobRepository) throws Exception {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        jobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor());
        jobLauncher.afterPropertiesSet();
        return jobLauncher;
    }
}
