package io.github.sakujj.nms.repository;

import io.github.sakujj.nms.entity.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface NewsRepository extends JpaRepository<News, UUID> {

    @Query(value = """
            UPDATE News SET
                title = :titleUpdated,
                text = :textUpdated,
                update_time = :updateTime
            WHERE id = :id
            """, nativeQuery = true)
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    int updateById(
            @Param("id") UUID id,
            @Param("textUpdated") String textUpdated,
            @Param("titleUpdated") String titleUpdated,
            @Param("updateTime") LocalDateTime updateTime
    );

    Page<News> findByTitleContaining(String content, Pageable pageable);

    Page<News> findByUsernameContaining(String content, Pageable pageable);
}
