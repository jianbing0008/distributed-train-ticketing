package com.jiawa.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jiawa.train.business.enums.RedisKeyPreEnum;
import com.jiawa.train.business.mapper.cust.SkTokenMapperCust;
import com.jiawa.train.common.resp.PageResp;
import com.jiawa.train.common.util.SnowUtil;
import com.jiawa.train.business.domain.SkToken;
import com.jiawa.train.business.domain.SkTokenExample;
import com.jiawa.train.business.mapper.SkTokenMapper;
import com.jiawa.train.business.req.SkTokenQueryReq;
import com.jiawa.train.business.req.SkTokenSaveReq;
import com.jiawa.train.business.resp.SkTokenQueryResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
/**
 * SkToken服务类，负责处理与SkToken相关的业务逻辑
 */
public class SkTokenService {

    @Autowired
    private SkTokenMapper skTokenMapper;

    @Autowired
    private DailyTrainSeatService dailyTrainSeatService;

    @Autowired
    private DailyTrainStationService dailyTrainStationService;

    @Autowired
    private SkTokenMapperCust skTokenMapperCust;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public void genDaily(Date date, String trainCode) {
        log.info("删除日期【{}】车次【{}】的令牌记录", DateUtil.formatDate(date), trainCode);
        SkTokenExample skTokenExample = new SkTokenExample();
        skTokenExample.createCriteria().andDateEqualTo(date).andTrainCodeEqualTo(trainCode);
        skTokenMapper.deleteByExample(skTokenExample);

        DateTime now = DateTime.now();
        SkToken skToken = new SkToken();
        skToken.setDate(date);
        skToken.setTrainCode(trainCode);
        skToken.setId(SnowUtil.getSnowflakeNextId());
        skToken.setCreateTime(now);
        skToken.setUpdateTime(now);

        int seatCount = dailyTrainSeatService.countSeat(date, trainCode);
        log.info("车次【{}】座位数：{}", trainCode, seatCount);

        long stationCount = dailyTrainStationService.countByTrainCode(date, trainCode);
        log.info("车次【{}】到站数：{}", trainCode, stationCount);

        // 3/4需要根据实际卖票比例来定，一趟火车最多可以卖（seatCount * stationCount）张火车票
        int count = (int) (seatCount * stationCount * 3 / 4);
        log.info("车次【{}】初始生成令牌数：{}", trainCode, count);
        skToken.setCount(count);

        skTokenMapper.insert(skToken);

        // 将初始令牌数量放入 Redis 缓存，确保是整数
        String skTokenCountKey = RedisKeyPreEnum.SK_TOKEN_COUNT + "-" + DateUtil.formatDate(date) + "-" + trainCode;
        redisTemplate.opsForValue().set(skTokenCountKey, String.valueOf(count), 60, TimeUnit.SECONDS);
    }

    public boolean validSkToken(Date date, String trainCode, Long memberId) {
        log.info("会员【{}】获取日期【{}】车次【{}】的令牌开始", memberId, DateUtil.formatDate(date), trainCode);

        // 先获取令牌锁，再校验令牌余量，防止机器人抢票，lockKey就是令牌，用来表示【谁能做什么】的一个凭证
        String lockKey = RedisKeyPreEnum.SK_TOKEN + "-" + DateUtil.formatDate(date) + "-" + trainCode + "-" + memberId;
        Boolean setIfAbsent = redisTemplate.opsForValue().setIfAbsent(lockKey, lockKey, 5, TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(setIfAbsent)) {
            log.info("恭喜，抢到令牌锁了！lockKey：{}", lockKey);
        } else {
            log.info("很遗憾，没抢到令牌锁！lockKey：{}", lockKey);
            return false;
        }

        String skTokenCountKey = RedisKeyPreEnum.SK_TOKEN_COUNT + "-" + DateUtil.formatDate(date) + "-" + trainCode;
        ValueOperations<String, String> valueOps = redisTemplate.opsForValue();
        String skTokenCountStr = valueOps.get(skTokenCountKey);
        if (skTokenCountStr != null) {
            log.info("缓存中有该车次令牌大闸的key：{}", skTokenCountKey);
            try {
                Long count = Long.parseLong(skTokenCountStr);
                count = valueOps.decrement(skTokenCountKey, 1);
                redisTemplate.expire(skTokenCountKey, 60, TimeUnit.SECONDS);
                if (count < 0L) {
                    log.error("获取令牌失败：{}", skTokenCountKey);
                    return false;
                } else {
                    log.info("获取令牌后，令牌余数：{}", count);
                    // 每获取5个令牌更新一次数据库
                    if (count % 5 == 0) {
                        skTokenMapperCust.decrease(date, trainCode, 5);
                    }
                    return true;
                }
            } catch (NumberFormatException e) {
                log.error("Redis 中的令牌数量格式不正确：{}", skTokenCountKey, e);
                return false;
            }
        } else {
            log.info("缓存中没有该车次令牌大闸的key：{}", skTokenCountKey);
            // 检查是否还有令牌
            SkTokenExample skTokenExample = new SkTokenExample();
            skTokenExample.createCriteria().andDateEqualTo(date).andTrainCodeEqualTo(trainCode);
            List<SkToken> tokenCountList = skTokenMapper.selectByExample(skTokenExample);
            if (CollUtil.isEmpty(tokenCountList)) {
                log.info("找不到日期【{}】车次【{}】的令牌记录", DateUtil.formatDate(date), trainCode);
                return false;
            }

            SkToken skToken = tokenCountList.get(0);
            if (skToken.getCount() <= 0) {
                log.info("日期【{}】车次【{}】的令牌余量为0", DateUtil.formatDate(date), trainCode);
                return false;
            }

            // 令牌还有余量
            // 令牌余数-1
            Integer count = skToken.getCount() - 1;
            skToken.setCount(count);
            log.info("将该车次令牌大闸放入缓存中，key: {}， count: {}", skTokenCountKey, count);
            // 不需要更新数据库，只要放缓存即可
            redisTemplate.opsForValue().set(skTokenCountKey, String.valueOf(count), 60, TimeUnit.SECONDS);
            return true;
        }
    }

    /**
     * 保存SkToken信息
     *
     * @param req SkToken保存请求对象，包含SkToken的基本信息
     */
    public void save(SkTokenSaveReq req) {
        // 获取当前时间，用于记录SkToken信息的创建和更新时间
        DateTime now = DateTime.now();
        // 将请求对象转换为SkToken对象，便于后续操作
        SkToken skToken = BeanUtil.copyProperties(req, SkToken.class);
        if (ObjectUtil.isNull(req.getId())) { // 判断是否为空，为空则是新增SkToken
            // 设置SkToken的会员ID，来源于登录会员上下文
            // 生成SkToken的唯一ID
            skToken.setId(SnowUtil.getSnowflakeNextId());
            // 设置SkToken信息的创建和更新时间为当前时间
            skToken.setCreateTime(now);
            skToken.setUpdateTime(now);
            // 插入SkToken信息到数据库
            skTokenMapper.insert(skToken);
        } else {  // 不为空则更新SkToken信息
            skToken.setUpdateTime(now);
            skTokenMapper.updateByPrimaryKey(skToken);
        }
    }

    /**
     * 查询SkToken列表
     *
     * @param req SkToken查询请求对象，可能包含SkToken的会员ID等查询条件
     */
    public PageResp<SkTokenQueryResp> queryList(SkTokenQueryReq req) {
        // 创建SkToken示例对象，用于构造查询条件
        SkTokenExample skTokenExample = new SkTokenExample();
        // 根据id倒序排序
        skTokenExample.setOrderByClause("id desc");
        // 创建查询条件对象
        SkTokenExample.Criteria criteria = skTokenExample.createCriteria();

        // 记录查询日志
        log.info("查询页码：{}", req.getPage());
        log.info("每页条数：{}", req.getSize());
        // 启用分页查询
        PageHelper.startPage(req.getPage(), req.getSize());
        // 根据构造的查询条件，从数据库中选择符合条件的SkToken信息
        List<SkToken> skTokenList = skTokenMapper.selectByExample(skTokenExample);

        // 创建PageInfo对象，用于获取分页信息
        PageInfo<SkToken> pageInfo = new PageInfo<>(skTokenList);
        // 记录分页信息日志
        log.info("总行数：{}", pageInfo.getTotal());
        log.info("总页数：{}", pageInfo.getPages());

        // 将查询结果列表转换为目标响应对象列表
        List<SkTokenQueryResp> list = BeanUtil.copyToList(skTokenList, SkTokenQueryResp.class);

        // 创建并组装分页响应对象
        PageResp<SkTokenQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    public void delete(Long id) {
        skTokenMapper.deleteByPrimaryKey(id);
    }
}
