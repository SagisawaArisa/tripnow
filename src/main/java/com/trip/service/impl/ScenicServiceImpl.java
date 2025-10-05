package com.trip.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.trip.dto.Result;
import com.trip.entity.Scenic;
import com.trip.entity.Blog;
import com.trip.mapper.ScenicMapper;
import com.trip.service.IScenicService;
import com.trip.service.IBlogService;
import com.trip.utils.CacheClient;
import com.trip.constants.SystemConstants;
import com.trip.constants.RedisScenicConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionBoostMode;
import co.elastic.clients.elasticsearch._types.query_dsl.FieldValueFactorModifier;
import co.elastic.clients.json.JsonData;

import jakarta.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Slf4j
@Service
public class ScenicServiceImpl extends ServiceImpl<ScenicMapper, Scenic> implements IScenicService {


    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private CacheClient cacheClient;

    @Resource
    private IBlogService blogService;

    @Resource
    private ElasticsearchClient esClient;

    @Override
    public Result queryById(Long id) {
        Scenic scenic = cacheClient
                .queryWithMutex(RedisScenicConstants.CACHE_SCENIC_PREFIX, id, Scenic.class, this::getById, RedisScenicConstants.CACHE_SCENIC_TTL.toMinutes(), TimeUnit.MINUTES);

        if (scenic == null) {
            return Result.fail("查询景区信息失败");
        }
        scenic.setBlogPreview(queryBlogPreview(id));
        return Result.ok(scenic);
    }

    @Override
    @Transactional
    public Result update(Scenic scenic) {
        Long id = scenic.getId();
        if (id == null) {
            return Result.fail("景区id不能为空");
        }
        // Cache Aside
        updateById(scenic);
        stringRedisTemplate.delete(RedisScenicConstants.getCacheScenicKey(id));
        return Result.ok();
    }

    @Override
    public Result queryScenicByType(Integer typeId, Integer current, Double x, Double y) {

        if (x == null || y == null) {
            Page<Scenic> page = query()
                    .eq("type_id", typeId)
                    .page(new Page<>(current, SystemConstants.DEFAULT_PAGE_SIZE));
            return Result.ok(page.getRecords());
        }

        int from = (current - 1) * SystemConstants.DEFAULT_PAGE_SIZE;
        int size = SystemConstants.DEFAULT_PAGE_SIZE;

        try {
            SearchResponse<Scenic> response = esClient.search(s -> s
                            .index("scenic")
                            .query(q -> q.term(t -> t
                                    .field("typeId")
                                    .value(typeId)
                            ))
                            .sort(sort -> sort.geoDistance(g -> g
                                    .field("location")
                                    .location(l -> l.latlon(ll -> ll.lat(y).lon(x)))
                                    .order(co.elastic.clients.elasticsearch._types.SortOrder.Asc)
                                    .unit(co.elastic.clients.elasticsearch._types.DistanceUnit.Kilometers)
                            ))
                            .from(from)
                            .size(size),
                    Scenic.class
            );

            List<Scenic> list = response.hits().hits().stream()
                    .map(h -> {
                        Scenic scenic = h.source();
                        if (scenic != null && h.sort() != null && !h.sort().isEmpty()) {
                            scenic.setDistance(h.sort().get(0).doubleValue());
                        }
                        return scenic;
                    })
                    .collect(Collectors.toList());

            return Result.ok(list);
        } catch (IOException e) {
            log.error("Search scenic by type failed", e);
            return Result.fail("Search failed");
        }
    }

    private List<Blog> queryBlogPreview(Long scenicId) {
        String key = RedisScenicConstants.getScenicBlogKey(scenicId);
        Set<String> blogIds = stringRedisTemplate.opsForZSet()
                .reverseRange(key, 0, RedisScenicConstants.SCENIC_BLOG_PREVIEW_FETCH - 1);
                // todo 如果返回数 < 预期, 异步清理 zset 中不存在的 id
        if (blogIds != null && !blogIds.isEmpty()) {
            List<Long> ids = blogIds.stream().map(Long::valueOf).toList();
            String idStr = StrUtil.join(",", ids);
            return blogService.query().in("id", ids)
                    .last("ORDER BY FIELD(id," + idStr + ")").list();
        }
        return blogService.query()
                .eq("scenic_id", scenicId)
                .orderByDesc("create_time")
                .last("limit " + RedisScenicConstants.SCENIC_BLOG_PREVIEW_FETCH)
                .list();
    }

    @Override
    public Result search(String key, String area, String sortBy, Double lat, Double lon, Integer page, Integer size) {
        try {
            SearchResponse<Scenic> response = esClient.search(s -> {
                        s.index("scenic");

                        // 构建基础查询
                        co.elastic.clients.elasticsearch._types.query_dsl.Query baseQuery;
                        if (StrUtil.isBlank(key) && StrUtil.isBlank(area)) {
                            baseQuery = co.elastic.clients.elasticsearch._types.query_dsl.Query.of(q -> q.matchAll(m -> m));
                        } else {
                            baseQuery = co.elastic.clients.elasticsearch._types.query_dsl.Query.of(q -> q.bool(b -> {
                                if (StrUtil.isNotBlank(key)) {
                                    b.must(m -> m.multiMatch(mm -> mm
                                            .fields("name", "area")
                                            .query(key)
                                    ));
                                }
                                if (StrUtil.isNotBlank(area)) {
                                    b.filter(f -> f.term(t -> t
                                            .field("area")
                                            .value(area)
                                    ));
                                }
                                return b;
                            }));
                        }

                        // 如果有关键词，设置最小相关度分数，过滤掉低相关结果
                        if (StrUtil.isNotBlank(key)) {
                            s.minScore(2.0);
                        }

                        // 应用 Function Score 或直接使用基础查询
                        if (StrUtil.isNotBlank(sortBy)) {
                            s.query(q -> q.functionScore(fs -> {
                                fs.query(baseQuery);
                                // 根据 sortBy 添加不同的打分函数
                                if ("distance".equals(sortBy) && lat != null && lon != null) {
                                    fs.functions(f -> f.gauss(g -> g
                                            .field("location")
                                            .placement(p -> p
                                                    .origin(JsonData.of(lat + "," + lon))
                                                    .scale(JsonData.of("10km"))
                                                    .offset(JsonData.of("0km"))
                                                    .decay(0.5)
                                            )
                                    ));
                                } else if ("sold".equals(sortBy)) {
                                    fs.functions(f -> f.fieldValueFactor(fv -> fv
                                            .field("sold")
                                            .modifier(FieldValueFactorModifier.Log1p)
                                            .factor(0.1)
                                    ));
                                } else if ("score".equals(sortBy)) {
                                    fs.functions(f -> f.fieldValueFactor(fv -> fv
                                            .field("score")
                                            .modifier(FieldValueFactorModifier.None)
                                            .factor(1.0)
                                    ));
                                }
                                // 设定 Boost Mode 为 Sum，累加相关性得分和函数得分
                                fs.boostMode(FunctionBoostMode.Sum);
                                return fs;
                            }));
                        } else {
                            s.query(baseQuery);
                        }

                        return s.from((page - 1) * size).size(size);
                    },
                    Scenic.class
            );

            List<Scenic> list = response.hits().hits().stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());

            return Result.ok(list);
        } catch (IOException e) {
            log.error("Search failed", e);
            return Result.fail("Search failed");
        }
    }

    @Override
    public Result queryHotScenicNearby(Double lat, Double lon, Integer distance, Integer page, Integer size) {
        try {
            SearchResponse<Scenic> response = esClient.search(s -> s
                            .index("scenic")
                            .query(q -> q.functionScore(fs -> fs
                                    // 只保留指定范围内的景点
                                    .query(bq -> bq.bool(b -> b
                                            .filter(f -> f.geoDistance(g -> g
                                                    .field("location")
                                                    .location(l -> l.latlon(ll -> ll.lat(lat).lon(lon)))
                                                    .distance(distance + "km")
                                            ))
                                    ))
                                    // 距离打分：高斯衰减函数
                                    // 距离越近分数越高，超过 scale 距离后分数衰减
                                    .functions(f -> f.gauss(g -> g
                                            .field("location")
                                            .placement(p -> p
                                                    .origin(JsonData.of(lat + "," + lon))
                                                    .scale(JsonData.of(distance + "km"))
                                                    .offset(JsonData.of("0km"))
                                                    .decay(0.5)
                                            )
                                    ))
                                    // 销量打分：log1p 平滑处理
                                    .functions(f -> f.fieldValueFactor(fv -> fv
                                            .field("sold")
                                            .modifier(FieldValueFactorModifier.Log1p)
                                            .factor(0.1)
                                    ))
                                    .boostMode(FunctionBoostMode.Sum)
                            ))
                            .from((page - 1) * size)
                            .size(size),
                    Scenic.class
            );

            List<Scenic> list = response.hits().hits().stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());

            return Result.ok(list);
        } catch (IOException e) {
            log.error("Search nearby failed", e);
            return Result.fail("Search nearby failed");
        }
    }
}
