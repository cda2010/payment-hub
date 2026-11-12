package com.m2i.pti.batch.reader;

import com.m2i.pti.batch.reader.validation.JobInterfaceListener;
import com.m2i.pti.domain.LigneChargementTransfert;
import com.m2i.pti.service.easyBatch.CSVTransfert;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

//@Configuration

public class BatchConfig {

    @Autowired
    StepBuilderFactory stepBuilderFactory;
    @Autowired
    JobBuilderFactory JobBuilderFactory;

    @Bean
    public ColumnRangePartitioner partitioner() {
        return new ColumnRangePartitioner();
    }
/*
    @Bean
    public PartitionHandler partitionHandler(Step slave) {
        TaskExecutorPartitionHandler taskExecutorPartitionHandler = new TaskExecutorPartitionHandler();
        taskExecutorPartitionHandler.setGridSize(4);
        taskExecutorPartitionHandler.setTaskExecutor(taskExecutor());
        taskExecutorPartitionHandler.setStep(slave);
        return taskExecutorPartitionHandler;
    }*/
/*
    @Bean
    public Step masterStep(Step slave) {
        return stepBuilderFactory.get("masterSTep").
                partitioner(slave.getName(), partitioner())
                .partitionHandler(partitionHandler(slave))
                .build();
    }*/

    Step getStep(ItemReader<CSVTransfert> transfertItemReader, ItemWriter<LigneChargementTransfert> writer) {
        Step step = stepBuilderFactory.get("chargement-fichier-batch")
                .<CSVTransfert, LigneChargementTransfert>chunk(10)
                .reader(transfertItemReader)
                .processor(processor())
                .writer(writer)
                //           .listener(steplistener())
                .build();
        return step;
    }

    @Bean
    @Qualifier(value = "chargementFromFile")
    Job insertFileinDB(ItemReader<CSVTransfert> transfertItemReader, ItemWriter<LigneChargementTransfert> writer) {
        Step stepSlave = getStep(transfertItemReader, writer);
        Job job = JobBuilderFactory.get("job")
                .incrementer(new RunIdIncrementer())
                .listener(jobFilelistener())
                .start(stepSlave)
                .build();
        return job;
    }

    @Bean
    @StepScope
    public StepInterfaceListener steplistener() {
        return new StepInterfaceListener();
    }

    @Bean
    @JobScope
    public JobInterfaceListener jobFilelistener() {
        return new JobInterfaceListener();
    }

    @Bean
    @StepScope
    FlatFileItemReader<CSVTransfert> flatFileItemReader(@Value("#{jobParameters['fileName']}") String fileName) throws UnexpectedInputException, ParseException, Exception {
        System.out.println("reader started");
        FlatFileItemReader<CSVTransfert> reader = new FlatFileItemReader<CSVTransfert>();
        //Set input file location
        reader.setResource(new FileSystemResource(fileName));
        //Set number of lines to skips. Use it if file has header rows.
        reader.setLinesToSkip(0);
        //Configure how each line will be parsed and mapped to different values
        reader.setLineMapper(new DefaultLineMapper() {
            {
                //3 columns in each row
                setLineTokenizer(new DelimitedLineTokenizer() {
                    {
                        setDelimiter(";");
                        setStrict(false);
                        setNames(new String[]{"nom", "prenom", "numeroTelephone", "montant", "narration"});
                    }
                });
                //Set values in Employee class
                setFieldSetMapper(new BeanWrapperFieldSetMapper<CSVTransfert>() {
                    {
                        setTargetType(CSVTransfert.class);
                    }
                });

            }
        });
//        reader.open(new ExecutionContext());
//        reader.read();

        return reader;

    }

    @Bean
    @StepScope
    public ItemWriter<LigneChargementTransfert> writer() {
        return new CustomerWriter();
    }

    @Bean
    @StepScope
    public CustomerProcessor processor() {
        return new CustomerProcessor();
    }
/*
    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setMaxPoolSize(20);
        taskExecutor.setCorePoolSize(20);
        taskExecutor.setQueueCapacity(1000);
        return taskExecutor;
    }**/

}
