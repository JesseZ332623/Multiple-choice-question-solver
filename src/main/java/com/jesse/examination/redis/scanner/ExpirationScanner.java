package com.jesse.examination.redis.scanner;

import com.jesse.examination.config.properties.PropertiesValue;
import com.jesse.examination.file.FileTransferServiceInterface;
import com.jesse.examination.question.dto.QuestionCorrectTimesDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 定期的扫描 Redis 数据库中的某些数据，
 * 在这些数据过期之前执行一些持久化的操作。
 */
@Slf4j
@Component
public class ExpirationScanner
{
    private final RedisTemplate<String, Object> redisTemplate;
    private final FileTransferServiceInterface  fileTransferService;
    private final PropertiesValue               propertiesValue;


    @Autowired
    public ExpirationScanner(
            RedisTemplate<String, Object> redisTemplate,
            FileTransferServiceInterface  fileTransferService,
            PropertiesValue               propertiesValue
    )
    {
        this.redisTemplate       = redisTemplate;
        this.fileTransferService = fileTransferService;
        this.propertiesValue     = propertiesValue;
    }

    /**
     * 每隔指定的一段时间, 由 Scheduled 注解的 fixedRate 属性指定（我这里是 1 分钟执行一次），
     * 去 Redis 中扫描由 CORRECT_TIMES_LIST_OF_ 开头的所有键对应的数据，
     * 检查它们的过期时间，对那些有效期不足的数据进行持久化操作。
     */
    @Scheduled(fixedRate = 60_000)
    public void scanExpiringCorrectTimesListKeys()
    {

        Set<String> matchedKeys
                = this.scanKeyByPattern("CORRECT_TIMES_LIST_OF_*");

        if (!matchedKeys.isEmpty()) {
            log.info("Matched keys: {}", matchedKeys);
        }
        else { log.info("Key CORRECT_TIMES_LIST_OF_* not matched."); }

        for (String key : matchedKeys)
        {
            // getExpire() 检查某个键的剩余过期时间，TimeUnit 指定时间单位
            Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);

            log.info(
                    "Key: {}, expire time reminder: {} seconds.",
                    key, ttl
            );

            /*
             * 若这个数据的过期时间已经不到 Session 过期时间的五分之一了，
             * 就得进行持久化操作。
             */
            if (ttl < this.propertiesValue.getSessionTimeOutSeconds() / 5)
            {
                // 提取用户名
                String userName
                        = key.substring(key.lastIndexOf('_') + 1);

                // 获取答对次数列表
                List<Object> value
                        = redisTemplate.opsForList()
                                       .range(key, 0, -1);

                log.info("Get data from key: {}", key);

                if (value != null)
                {
                    for (Object obj : value)
                    {
                        @SuppressWarnings(value = "unchecked")
                        var questionCorrectTimesList = (List<QuestionCorrectTimesDTO>) obj;

                        // 存入文件系统中去
                        this.fileTransferService.saveUserCorrectTimesDataFile(
                                userName, questionCorrectTimesList
                        );

                        log.info("Sava questionCorrectTimesList to file system complete.");
                    }
                }
            }
        }
    }

    /**
     * 使用指定的通配符，扫描出所有匹配的键，返回到一个集合之中。
     *
     * @param pattern Redis 键样式（比如：<code>CORRECT_TIMES_LIST_OF_*</code>）
     *
     * @return 扫描到的所有与 pattern 匹配的键的集合（若没有扫描到就返回空集合）
     */
    private Set<String> scanKeyByPattern(String pattern)
    {
        return this.redisTemplate.execute(
                (RedisCallback<Set<String>>)
                connection -> {
                    Set<String> matchedKeys = new HashSet<>();
                    ScanOptions options
                            = ScanOptions.scanOptions()
                                         .match(pattern)  // 设置键样式
                                         .count(100)      // 每回扫描 100 条
                                         .build();

                    /*
                     * 将准备好的命令选项 options 交给 Redis 去执行，
                     * 扫描结果在 cursor 中。
                    */
                    try (Cursor<byte[]> cursor = connection.keyCommands().scan(options))
                    {
                        while (cursor.hasNext()) {
                            matchedKeys.add(new String(cursor.next()));
                        }
                    }
                    catch (Exception exception)
                    {
                        log.error(
                                "Redis scan error for pattern: {}. Cause: {}.",
                                pattern, exception.getMessage()
                        );
                    }

                    return matchedKeys;
                }
        );
    }
}
