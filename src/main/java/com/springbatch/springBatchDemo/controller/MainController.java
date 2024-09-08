package com.springbatch.springBatchDemo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@ResponseBody
@RequiredArgsConstructor
public class MainController {

    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;

    /**
     * api를 이용한 베치처리
     * @param value 중복된 값의 파라미터를 받을 경우 베치에서 exception을 터트려준다
     * @return
     * @throws Exception
     */
    @GetMapping("/first")
    public String firstApi(@RequestParam("value") String value) throws Exception{
        JobParameters jobParameter = new JobParametersBuilder()
                .addString("date",value)
                        .toJobParameters();

        jobLauncher.run(jobRegistry.getJob("firstJob"), jobParameter);
        return "ok";
    }

    /**
     * 베치에 조건을 추가하여 조건에 맞는 데이터만 받아온다
     * @param value
     * @return
     * @throws Exception
     */
    @GetMapping("/second")
    public String SecondApi(@RequestParam("value") String value) throws Exception{
        JobParameters jobParameter = new JobParametersBuilder()
                .addString("date",value)
                .toJobParameters();

        jobLauncher.run(jobRegistry.getJob("SecondJob"), jobParameter);
        return "ok";
    }
}