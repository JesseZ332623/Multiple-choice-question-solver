package com.jesse.examination.hibernate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueryStatisticsDTO
{
    long queryTimes;             // 查询次数
    long entityLoadTimes;        // 实体加载次数
    long setLoadTimes;           // 集合加载次数
    long L2CacheHitTimes;        // 二级缓存命中次数
    long queryCacheHitTimes;     // 查询缓存命中次数
    long connectObtainTimes;     // 连接获取次数
}
