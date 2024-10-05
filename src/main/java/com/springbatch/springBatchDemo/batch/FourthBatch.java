package com.springbatch.springBatchDemo.batch;

import com.springbatch.springBatchDemo.entity.AfterEntity;
import com.springbatch.springBatchDemo.repository.AfterRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.batch.core.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.FileNotFoundException;
import java.io.IOException;

@Configuration
@RequiredArgsConstructor
public class FourthBatch {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final AfterRepository afterRepository;

    @Bean
    public Job fourthJob(){
        return new JobBuilder("fourthJob",jobRepository)
                .start(fourthStep())
                /**
                 * .next(##step)다음으로 실행할 step을 next로 추가해줄수있다
                 * 하지만 앞에 있는 step이 실패하면 후에 실행해야할 step은 실행을 하지 못한다.
                 *
                 * 이를 방지하기위해
                 * on("*").to(step)->앞선 step이 실해하든 성공하든 다음에 등록된 step을 실행
                 * .from(stepA).on("FAILED").to(step)->실패해도 실행
                 * .from(stepA).on("COMPLETED").to(step)->성공하면 실행
                 * .end()마무리
                  *
                 * 이도 step과 같이 JobExecutionListener를 통해 job실행 전과 후로 특정작업을 할 수 있다.
                 * .listener(jobExecutionListener())
                 */
                .build();
    }

    //@Bean
    public JobExecutionListener jobExecutionListener(){
        return new JobExecutionListener() {
            @Override
            public void beforeJob(JobExecution jobExecution) {
                JobExecutionListener.super.beforeJob(jobExecution);
            }

            @Override
            public void afterJob(JobExecution jobExecution) {
                JobExecutionListener.super.afterJob(jobExecution);
            }
        };
    }

    @Bean
    public Step fourthStep(){
        return new StepBuilder("fourthStep",jobRepository)
                .<Row, AfterEntity>chunk(10,platformTransactionManager)
                .reader(excelReader())
                .processor(fourthProcessor())
                .writer(fourthAfterWriter())
                /**
                .faultTolerant()
                .skip(Exception.class) 스킵할 에러를 설정
                .noSkip(FileNotFoundException.class) 스킵하지 않을 에러를 설정
                .noSkip(IOException.class)
                .skipLimit(10)

                 or

                 skipPolicy를 이용해 스큽처리를 할 수 도 있다
                 .skipPolicy(customSkipPolicy)


                 retry 실행중 에러가 발생해도 다시 시도할수 있도록
                 .retryLimit(3)
                 .retry(Exception.class)retry할 예외

                 rollback 트렌잭션 실행x
                 .noRollBack(Exception.class)

                step listener step이 실행돠기 전과 후에 특정작업을 실행할수있게
                 StepExecutionListener를 사용하여 beforestep과 afterstep을 구현하여 실행
                 .listener(stepExecutionListener)
                **/
                .build();
    }

    @Bean
    public StepExecutionListener stepExecutionListener(){
        return new StepExecutionListener() {
            @Override
            public void beforeStep(StepExecution stepExecution) {
                StepExecutionListener.super.beforeStep(stepExecution);
            }

            @Override
            public ExitStatus afterStep(StepExecution stepExecution) {
                return StepExecutionListener.super.afterStep(stepExecution);
            }
        };
    }
    @Bean
    public ItemStreamReader<Row> excelReader(){
        return new ExcelRowReader("/Users/jeonbyeong-il/Downloads/test.xlsx");
    }

    @Bean
    public ItemProcessor<Row, AfterEntity> fourthProcessor(){
        return new ItemProcessor<Row, AfterEntity>() {
            @Override
            public AfterEntity process(Row item) throws Exception {
                AfterEntity afterEntity = new AfterEntity();
                afterEntity.setUserName(item.getCell(0).getStringCellValue());
                return afterEntity;
            }
        };
    }

    @Bean
    public RepositoryItemWriter<AfterEntity> fourthAfterWriter(){
        return new RepositoryItemWriterBuilder<AfterEntity>()
                .repository(afterRepository)
                .methodName("save")
                .build();
    }
}
