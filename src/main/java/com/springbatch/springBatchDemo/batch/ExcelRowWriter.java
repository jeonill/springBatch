package com.springbatch.springBatchDemo.batch;

import com.springbatch.springBatchDemo.entity.BeforeEntity;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamWriter;

import java.io.FileOutputStream;
import java.io.IOException;

public class ExcelRowWriter implements ItemStreamWriter<BeforeEntity> {
    private final String filePath;
    private Workbook workbook;
    private Sheet  sheet;
    private int currentRowNumber;
    private boolean isClosed;
    public ExcelRowWriter(String filePath){
        this.filePath = filePath;
        this.isClosed = false;
        currentRowNumber = 0;
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("Sheet1");
    }

    @Override
    public void write(Chunk<? extends BeforeEntity> chunk) throws Exception {
        for (BeforeEntity entity : chunk){
            Row row = sheet.createRow(currentRowNumber++);
            row.createCell(0).setCellValue(entity.getUserName());
        }
    }

    @Override
    public void close() throws ItemStreamException {
        if (isClosed){
            return;
        }

        try (FileOutputStream fileOut = new FileOutputStream(filePath)){
            workbook.write(fileOut);
        }catch (IOException e){
            throw new ItemStreamException(e);
        }finally {
            try {
                workbook.close();
            }catch (IOException e){
                throw new ItemStreamException(e);
            } finally {
                isClosed = true;
            }
        }
    }
}
