package com.dsw.trabalho.minimalList.repository;

import com.dsw.trabalho.minimalList.model.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ContentRepository extends JpaRepository<Content, Integer> {
    @Query(value = "SELECT * FROM contents WHERE name LIKE %?1% OR title LIKE %?1%", nativeQuery = true)
    List<Content> findAllByNameOrTitle(String search);
}
