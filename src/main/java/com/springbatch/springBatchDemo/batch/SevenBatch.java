package com.springbatch.springBatchDemo.batch;

import com.springbatch.springBatchDemo.entity.AfterEntity;
import com.springbatch.springBatchDemo.entity.BeforeEntity;
import com.springbatch.springBatchDemo.entity.CustomBeforeRowMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SevenBatch {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    public final DataSource dataSource;

    private int chunkSize;
    private int poolSize;
    int number = 1;
    private static final String SEVEN_JOB = "sevenJob";

    //유동적인 사이즈조정을 위해
    @Value("${chunkSize:100}")
    public void setChunkSize(int chunkSize){
        this.chunkSize = chunkSize;
    }
    @Value("${poolSize:10}")
    public void setPoolSize(int poolSize){
        this.poolSize = poolSize;
    }

    /**
     * 쓰레드 병렬처리를 위한 TaskExecutor
     * @return
     */
    @Bean(name = SEVEN_JOB + "_executor")
    public TaskExecutor executor(){

        ThreadPoolTaskExecutor threadPoolExecutor = new ThreadPoolTaskExecutor();
        threadPoolExecutor.setCorePoolSize(poolSize);
        threadPoolExecutor.setMaxPoolSize(poolSize);
        threadPoolExecutor.setThreadNamePrefix("multi-thread-");
        threadPoolExecutor.setWaitForTasksToCompleteOnShutdown(true);
        threadPoolExecutor.initialize();
        return threadPoolExecutor;
    }

    @Bean(name = SEVEN_JOB)
    public Job SevenJob() throws Exception {
        return new JobBuilder(SEVEN_JOB,jobRepository)
                .start(sevenStep())
                //.preventRestart()//실패시 재시작해줌
                .build();
    }

    @Bean(name = SEVEN_JOB + "_step")
    @JobScope//job 병렬처리를 위하여
    public Step sevenStep() throws Exception {
        return new StepBuilder(SEVEN_JOB + "_step", jobRepository)
                .<BeforeEntity, AfterEntity>chunk(10, platformTransactionManager)
                .reader(beforeSevenReader())
                .processor(sevenMiddleProcessor())
                .taskExecutor(executor())//병렬처리를 위하여 별도의 쓰레드를 사용하는 taskExcutor
                .writer(afterSevenWriter())
                .build();
    }

    @Bean
    @StepScope//Step 병렬처리를 위하여
    public JdbcPagingItemReader<BeforeEntity> beforeSevenReader() throws Exception{
        JdbcPagingItemReader<BeforeEntity> jdbcPagingItemReader = new JdbcPagingItemReaderBuilder<BeforeEntity>()
                .name("beforeSevenReader")
                .dataSource(dataSource)
                .selectClause("SELECT id, userName")
                .fromClause("FROM BeforeEntity")
                .sortKeys(Map.of("id", Order.ASCENDING))
                .rowMapper(new CustomBeforeRowMapper())
                .pageSize(10)
                .saveState(false)//병렬처리시 스프링 배치에서의 상태변동을 보장할 수 없게 되므로
                .build();
        return jdbcPagingItemReader;
    }

    @Bean
    public ItemProcessor<BeforeEntity, AfterEntity> sevenMiddleProcessor(){
        return new ItemProcessor<BeforeEntity, AfterEntity>() {
            @Override
            public AfterEntity process(BeforeEntity item) throws Exception {
                Thread.sleep(1000);//병렬처리 확인을 위한 쓰레드지연
                AfterEntity afterEntity = new AfterEntity();
                afterEntity.setUserName(item.getUserName());
                log.info("threadId:{}",item.getId());
                return afterEntity;
            }
        };
    }

    @Bean
    @StepScope
    public JdbcBatchItemWriter<AfterEntity> afterSevenWriter() {

        String sql = "INSERT INTO AfterEntity (userName) VALUES (:userName)";

        return new JdbcBatchItemWriterBuilder<AfterEntity>()
                .dataSource(dataSource)
                .sql(sql)
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .build();
    }
}
