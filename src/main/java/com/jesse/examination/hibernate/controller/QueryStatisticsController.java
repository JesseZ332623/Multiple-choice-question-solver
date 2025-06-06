package com.jesse.examination.hibernate.controller;

import com.jesse.examination.hibernate.dto.QueryStatisticsDTO;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(path = "/api/hibernate/")
public class QueryStatisticsController
{
    // @PersistenceContext
    final private EntityManager  entityManager;
    final private Statistics     statistics;

    public QueryStatisticsController(EntityManager entityManager)
    {
        this.entityManager = entityManager;

        SessionFactory sessionFactory
                     = this.entityManager.getEntityManagerFactory()
                                         .unwrap(SessionFactory.class);

        this.statistics = sessionFactory.getStatistics();

        if (!this.statistics.isStatisticsEnabled())
        {
            // log.warn("统计功能未启用！将手动开启。");
            statistics.setStatisticsEnabled(true);
        }
    }

    @PutMapping(path = "clear_statistics_data")
    public ResponseEntity<String> clearStatisticsData()
    {
        this.statistics.clear();

        return ResponseEntity.ok(
                "Clear statistics data complete."
        );
    }

    @GetMapping(path = "query_statistics")
    public ResponseEntity<QueryStatisticsDTO>
    showQueryStatistics()
    {
        return ResponseEntity.ok(
                new QueryStatisticsDTO(
                    this.statistics.getQueryExecutionCount(),
                    this.statistics.getEntityLoadCount(),
                    this.statistics.getCollectionLoadCount(),
                    this.statistics.getSecondLevelCacheHitCount(),
                    this.statistics.getQueryCacheHitCount(),
                    this.statistics.getConnectCount()
                )
        );
    }
}