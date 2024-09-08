package com.springbatch.springBatchDemo.repository;

import com.springbatch.springBatchDemo.entity.AfterEntity;
import com.springbatch.springBatchDemo.entity.BeforeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AfterRepository extends JpaRepository<AfterEntity, Long> {
}
