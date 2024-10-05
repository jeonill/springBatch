package com.springbatch.springBatchDemo.batch;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.batch.item.*;
import org.springframework.web.client.RestTemplate;

public class CustomItemStreamReaderImpl implements ItemStreamReader {

    private final RestTemplate restTemplate;
    private int currentId;
    private final String CURRENT_ID_KEY = "current.call.id";
    private final String API_URL = "http~~~~api주소";

    public CustomItemStreamReaderImpl (RestTemplate restTemplate){
        this.currentId = 0;
        this.restTemplate = restTemplate;
    }

    @Override//시작할시 단 한번실행
    public void open(ExecutionContext executionContext) throws ItemStreamException {
       if(executionContext.containsKey(CURRENT_ID_KEY)) { //현재 어디까지 데이터를 받았는지 파악할 수 있다.
           currentId = executionContext.getInt(CURRENT_ID_KEY);
       }
    }

    @Override//
    public String read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        currentId++;

        String url = API_URL + currentId;
        String response = restTemplate.getForObject(url, String.class);

        if(response == null){
            return null;
        }
        return response;
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        executionContext.putInt(CURRENT_ID_KEY, currentId);
    }

    @Override//배치가 끝나고 마지막에 단 한번 실행
    public void close() throws ItemStreamException {
        ItemStreamReader.super.close();
    }
}
