package com.jesse.examination.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpMethod;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 请求响应体模板类。
 *
 * @param <T> 响应体数据类型
 *
 * <p>
 *      作为重构的第一步，
 *      就是把所有 restful 端点的响应标准化，
 *      为后续的前后端分离做好基础。
 * </p>
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class APIResponse<T>
{
    /** 响应时间戳（默认是构造本响应体那一刻的时间）*/
    private final long TIME_STAMP = Instant.now().toEpochMilli();

    /** 响应消息 */
    private String message;

    /**
     * 响应数据，
     * 使用 @JsonInclude(JsonInclude.Include.NON_NULL) 注解，</br>
     * 在 data == null 的情况下，这个字段就不会序列化到 JSON 中去。
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    /**
     * 响应元数据，
     * 使用 @JsonInclude(JsonInclude.Include.NON_EMPTY) 注解，</br>
     * metadata 为 null、ImmutableCollections.EMPTY_MAP、
     * 或者其他没有任何元素的空 Map 时，这个字段不会被序列化到 JSON 中去。
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, Object> metadata = new HashMap<>();

    /**
     * 检查当前响应体中有没有键 _links 映射的 HATEOAS 链接表，
     * 有转换后返回，没有则创建一个后返回。
     */
    private Map<String, Object> getOrCreateLinksMap()
    {
        Object linksObj = this.getMetadata().get("_links");

        if (linksObj instanceof Map)
        {
            @SuppressWarnings(value = "unchecked")
            Map<String, Object> linksMap = (Map<String, Object>) linksObj;

            return linksMap;
        }
        else
        {
            Map<String, Object> linksMap = new HashMap<>();
            this.getMetadata().put("_links", linksMap);

            return linksMap;
        }
    }

    /**
     * 创建单个 HATEOAS 链接表。
     *
     * @param href   链接字符串
     * @param method 请求方法
     */
    private Map<String, String>
    createLinkObject(String href, String method)
    {
        Map<String, String> link = new HashMap<>();

        link.put("href", href);
        link.put("method", method);

        return link;
    }

    /**
     * 在响应体中添加分页元数据。
     *
     * @param page        第几页？
     * @param size        一页几条数据？
     * @param totalItems  总共几条数据？
     *
     * @return 添加完分页元数据的响应体
     */
    public APIResponse<T>
    withPagination(int page, int size, long totalItems)
    {
        Map<String, Object> pagination = new HashMap<>();

        pagination.put("page", page);
        pagination.put("size", size);
        pagination.put("totalItem", totalItems);

        this.getMetadata().put("pagination", pagination);

        return this;
    }

    /**
     * 在响应体元数据中添加 HATEOAS 元数据。
     * <p>
     *      HATEOAS 全称为 Hypermedia as the Engine of Application State，
     *      作为 REST 架构的核心约束之一，它是一种可以让响应体能够自我描述的理念
     *     （即响应体不仅包括了相关数据，还包括了相关操作的链接）。
     * </p>
     * <p>假设没有添加 HATEOAS 的响应体是这样的：</p>
     * <pre><code>
     * {
     *     "message": "Query score record id = 300 success!",
     *     "data": {
     *         "scoreId": 300,
     *         "userId": 1,
     *         "userName": "Jesse",
     *         "submitDate": [2025, 2, 3, 10, 22, 6],
     *         "correctCount": 10,
     *         "errorCount": 26,
     *         "noAnswerCount": 38
     *     },
     *     "statues": "OK",
     *     "time_STAMP": 1750942236538
     * }
     * </code></pre>
     *
     * <p>
     *     那么添加了 HATEOAS 元数据的响应体可能是这样的
     *     （多出了相关操作的链接，前端可以更方便地执行相关操作，不需要再手动的拼接 URL）：
     * </p>
     * <pre><code>
     * {
     *     "message": "Query score record id = 300 success!",
     *     "data": {
     *         "scoreId": 300,
     *         "userId": 1,
     *         "userName": "Jesse",
     *         "submitDate": [2025, 2, 3, 10, 22, 6],
     *         "correctCount": 10,
     *         "errorCount": 26,
     *         "noAnswerCount": 38
     *     },
     *     "metadata": {
     *          "_links": {
     *              "self": {
     *                  "href": "/api/score_record?id=300",
     *                  "method": "GET"
     *              },
     *              "next": {
     *                  "href": "/api/score_record?id=301",
     *                  "method": "GET"
     *              },
     *              "previous" : {
     *                  "href": "/api/score_record?id=299",
     *                  "method": "GET"
     *              },
     *              "update" : {
     *                  "href" : "/api/score_record?id=300",
     *                  "method" : "PUT"
     *              },
     *              "related": [
     *              {
     *                  "href": "/api/score_record/rank",
     *                  "method": "GET"
     *              },
     *              {
     *                  ....
     *              }
     *          ]
     *     }
     *     "statues": "OK",
     *     "time_STAMP": 1750942236538
     * }
     * </code></pre>
     *
     * <p>
     *     HATEOAS 元数据添加规则：
     *     <ol>
     *         <li>首次添加不存在的链接   -> 存储为单对象</li>
     *         <li>再次添加简述相同的链接 -> 转换为数组后再存储</li>
     *         <li>后续添加简述相同的链接 -> 追加到数组</li>
     *     </ol>
     * </p>
     *
     * @param rel    链接简述
     * @param href   链接字符串
     * @param method 请求方法
     *
     * @return 添加完 HATEOAS 元数据的响应体
     */
    public APIResponse<T>
    withLink(String rel, String href, HttpMethod method)
    {
        Map<String, Object> linksMap = this.getOrCreateLinksMap();

        Object existLinkingMap = linksMap.get(rel);

        switch (existLinkingMap)
        {
            case null -> linksMap.put(rel, this.createLinkObject(href, method.name()));

            case Map<?, ?> ignored ->
            {
                List<Object> linksList = new ArrayList<>();

                linksList.add(existLinkingMap);
                linksList.add(this.createLinkObject(href, method.name()));

                linksMap.put(rel, linksList);
            }
            case List<?> ignored ->
            {
                @SuppressWarnings(value = "unchecked")
                List<Object> linksList = (List<Object>) existLinkingMap;

                linksList.add(this.createLinkObject(href, method.name()));
            }
            default -> {}
        }

        return this;
    }

    /**
     * 在响应体元数据中添加 HATEOAS 元数据（默认使用 GET 方法）。
     *
     * @param rel    链接简述
     * @param href   链接字符串
     */
    public APIResponse<T>
    withLink(String rel, String href) {
        return this.withLink(rel, href, HttpMethod.GET);
    }
}