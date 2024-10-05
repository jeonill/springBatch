package com.springbatch.springBatchDemo.batch;

import com.springbatch.springBatchDemo.entity.AfterEntity;
import com.springbatch.springBatchDemo.entity.BeforeEntity;
import com.springbatch.springBatchDemo.repository.AfterRepository;
import com.springbatch.springBatchDemo.repository.BeforeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Map;

@Configuration
public class FirstBatch {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final BeforeRepository beforeRepository;
    private final AfterRepository afterRepository;

    @Autowired
    public FirstBatch(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager, BeforeRepository beforeRepository, AfterRepository afterRepository) {
        this.jobRepository = jobRepository;
        this.platformTransactionManager = platformTransactionManager;
        this.beforeRepository = beforeRepository;
        this.afterRepository = afterRepository;
    }

    @Bean
    public Job firstJob(){
        return new JobBuilder("firstJob", jobRepository)
                .start(firstStep())
                .build();
    }

    @Bean
    public Step firstStep(){
        return new StepBuilder("firstStep",jobRepository)
                .<BeforeEntity, AfterEntity> chunk(10, platformTransactionManager)//단위로 끊어 데이터를 읽어 드린다. 대량의 작업을 할때 유용하다(너무작게하면 I/O에서의 오버헤드로인한 문제가 발새하고, 너무 크면 자원사용의 부담으로 이어진다.)
                .reader(beforeReader())
                .processor(middleProcessor())
                .writer(afterWriter())
                .build();
    }

    //data를 읽어노는과정
    @Bean //인터페이스별로 다양한 구현체가 존재한다. jpa를 사용함으로써 RepositoryItemReader를 사용
    public RepositoryItemReader<BeforeEntity> beforeReader(){
        return  new RepositoryItemReaderBuilder<BeforeEntity>()
                .name("beforeReader")
                .pageSize(10)
                .methodName("findAll")
                .repository(beforeRepository)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    @Bean//읽어온 데이터를 처리하는 과정으로 큰 작업이 아닌이상 거의 사용하지 않는다.
    public ItemProcessor<BeforeEntity, AfterEntity> middleProcessor(){
        return new ItemProcessor<BeforeEntity, AfterEntity>() {
            @Override
            public AfterEntity process(BeforeEntity item) throws Exception {
                AfterEntity afterEntity = new AfterEntity();
                afterEntity.setUserName(item.getUserName());

                return afterEntity;
            }
        };
    }

    @Bean//
    public RepositoryItemWriter<AfterEntity> afterWriter() {
        return new RepositoryItemWriterBuilder<AfterEntity>()
                .repository(afterRepository)
                .methodName("save")//리포지토리에서 사용할 메소드이름
                .build();
    }

}
