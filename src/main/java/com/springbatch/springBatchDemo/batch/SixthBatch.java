package com.springbatch.springBatchDemo.batch;

import com.springbatch.springBatchDemo.entity.AfterEntity;
import com.springbatch.springBatchDemo.entity.BeforeEntity;
import com.springbatch.springBatchDemo.entity.CustomBeforeRowMapper;
import com.springbatch.springBatchDemo.repository.AfterRepository;
import com.springbatch.springBatchDemo.repository.BeforeRepository;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
@Slf4j
@Configuration
@RequiredArgsConstructor
public class SixthBatch {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final BeforeRepository beforeRepository;
    private final AfterRepository afterRepository;
    public final DataSource dataSource;

    @Bean
    public JobExecutionListener jobExecutionListener(){
        return new JobExecutionListener() {

            private LocalDateTime startTime;
            
            
            @Override
            public void beforeJob(JobExecution jobExecution) {
                startTime = LocalDateTime.now();
            }

            @Override
            public void afterJob(JobExecution jobExecution) {
                LocalDateTime endTime = LocalDateTime.now();

                long nanos = ChronoUnit.NANOS.between(startTime, endTime);
                double seconds = nanos / 1_000_000_000.0;

                log.info("실행시간 : {}초",seconds);
            }
        };
    }
    
    @Bean
    public Job SixthJob() throws Exception {
        return new JobBuilder("sixthJob",jobRepository)
                .start(sixthStep())
                .listener(jobExecutionListener())
                .build();
    }

    @Bean
    public Step sixthStep() throws Exception {
        return new StepBuilder("sixthStep",jobRepository)
                .<BeforeEntity, AfterEntity>chunk(10,platformTransactionManager)
                .reader(beforeSixthReader())
                .processor(sixthMiddleProcessor())
                .writer(afterSixthWriter())
                .build();
    }

    /*@Bean
    public RepositoryItemReader<BeforeEntity> beforeSixthReader(){
        return  new RepositoryItemReaderBuilder<BeforeEntity>()
                .name("beforeReader")
                .pageSize(10)
                .methodName("findAll")
                .repository(beforeRepository)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }*/

    @Bean
    public JdbcPagingItemReader<BeforeEntity> beforeSixthReader() throws Exception{
        JdbcPagingItemReader<BeforeEntity> jdbcPagingItemReader = new JdbcPagingItemReaderBuilder<BeforeEntity>()
                .name("beforeSixthReader")
                .dataSource(dataSource)
                .selectClause("SELECT id, userName")
                .fromClause("FROM BeforeEntity")
                .sortKeys(Map.of("id", Order.ASCENDING))
                .rowMapper(new CustomBeforeRowMapper())
                .pageSize(10)
                .build();
        return jdbcPagingItemReader;
    }

    @Bean//읽어온 데이터를 처리하는 과정으로 큰 작업이 아닌이상 거의 사용하지 않는다.
    public ItemProcessor<BeforeEntity, AfterEntity> sixthMiddleProcessor(){
        return new ItemProcessor<BeforeEntity, AfterEntity>() {
            @Override
            public AfterEntity process(BeforeEntity item) throws Exception {
                AfterEntity afterEntity = new AfterEntity();
                afterEntity.setUserName(item.getUserName());

                return afterEntity;
            }
        };
    }

    /*@Bean
    public RepositoryItemWriter<AfterEntity> afterSixthWriter() {

        return new RepositoryItemWriterBuilder<AfterEntity>()
                .repository(afterRepository)
                .methodName("save")
                .build();
    }*/

    @Bean
    public JdbcBatchItemWriter<AfterEntity> afterSixthWriter() {

        String sql = "INSERT INTO AfterEntity (username) VALUES (:username)";

        return new JdbcBatchItemWriterBuilder<AfterEntity>()
                .dataSource(dataSource)
                .sql(sql)
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .build();
    }
}
