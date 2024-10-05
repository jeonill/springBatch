package com.springbatch.springBatchDemo.entity;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomBeforeRowMapper implements RowMapper<BeforeEntity> {

    @Override
    public BeforeEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        BeforeEntity beforeEntity = new BeforeEntity();

        beforeEntity.setId(rs.getLong("id"));
        beforeEntity.setUserName(rs.getString("userName"));
        return beforeEntity;
    }
}
