package com.cb.repository;

import com.cb.model.FileMeta;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileMetaRepository extends CrudRepository<FileMeta, Integer> {
}
