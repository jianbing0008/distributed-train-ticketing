# 12306购票系统

## 项目简介

基于Spring Cloud Alibaba微服务架构的高铁票务系统，实现**单日千万级余票查询**与**秒级万级购票请求处理**。系统通过**多级缓存、异步削峰、分布式事务**等技术，解决动态库存、选座逻辑、超卖等业务难点，并针对12306典型高并发场景进行深度优化。

------

## 技术栈

| 层级         | 技术选型                                                     |
| ------------ | ------------------------------------------------------------ |
| **后端框架** | SpringBoot 3.0.0 + SpringCloud Alibaba 2022.0.0 + MyBatis 3.X |
| **前端框架** | Vue3 + Ant Design Vue                                        |
| **中间件**   | Redis（分布式缓存）、RocketMQ（异步削峰）、Nacos（注册/配置中心）、Seata（分布式事务）、Sentinel（限流熔断） |
| **数据库**   | MySQL 8.0（分库分表）+ 读写分离                              |

## 高并发场景优化

| 技术方案             | 实现效果                                          |
| -------------------- | ------------------------------------------------- |
| **双级验证码削峰**   | 前端验证码分散瞬时请求，后端验证码过滤机器人流量  |
| **令牌桶限流**       | 控制每秒处理量从10万→1万，响应时间<50ms           |
| **Redisson分布式锁** | 解决2000并发抢1000票场景下的超卖问题              |
| **RocketMQ异步排队** | QPS提升约25倍（从40 QPS→1000 QPS）                |
| **多级缓存架构**     | 余票查询响应<10ms（本地缓存+Redis+MySQL三级架构） |

## JMeter压测

### 测试环境

- 机器配置：i7-12700 , 16G
- 并发场景：500线程持续压测

### 性能对比

| 优化阶段             | QPS  | 平均响应时间 | 错误率 |
| -------------------- | ---- | ------------ | ------ |
| 基础版本（无中间件） | 42   | 2350ms       | 31%    |
| 加入Redis缓存        | 310  | 480ms        | 1.6%   |
| 增加MQ异步处理       | 1024 | 89ms         | 0%     |

### 关键指标提升

- **吞吐量提升24倍**：从42 QPS→1024QPS
- **响应时间降低98%**：从2350ms→89ms

## 流程图

```mermaid
flowchart TD
    subgraph 用户注册和登录流程
        User[用户] -->|访问登录/注册页面| LoginRegisterPage[登录/注册页面]
        LoginRegisterPage -->|发送验证码| SMS[短信服务]
        LoginRegisterPage -->|用户注册| RegisterProcess[注册流程]
        LoginRegisterPage -->|用户登录| LoginProcess[登录流程]
        LoginProcess -->|生成和验证JWT令牌| JWT[JWT令牌]
    end

    subgraph 车票预订流程
        User -->|搜索可用列车| SearchTrains[搜索列车]
        SearchTrains -->|选择列车、车站和日期| SelectTrain[选择列车]
        SelectTrain -->|检查座位可用性| CheckSeats[检查座位]
        CheckSeats -->|显示座位选项| DisplaySeats[显示座位选项]
        DisplaySeats -->|用户选择座位| UserSelectSeat[用户选择座位]
        UserSelectSeat -->|验证座位可用性| ValidateSeats[验证座位]
        ValidateSeats -->|分配座位| AllocateSeats[分配座位]
        AllocateSeats -->|确认订单和支付| ConfirmOrder[确认订单]
        ConfirmOrder -->|更新订单状态| UpdateOrderStatus[更新订单状态]
        UpdateOrderStatus -->|生成车票| GenerateTicket[生成车票]
        GenerateTicket -->|通知用户| NotifyUser[通知用户]
    end

    subgraph 每日列车时刻表生成
        BatchJobTrigger[批量作业触发] -->|生成每日列车时刻表| GenerateDailySchedule[生成每日时刻表]
        GenerateDailySchedule -->|计算座位可用性和初始令牌分配| CalculateSeats[计算座位]
        CalculateSeats -->|更新数据库| UpdateDatabase[更新数据库]
    end

    subgraph 座位选择和分配
        UserSelectSeat -->|验证座位可用性| ValidateSeats
        ValidateSeats -->|根据用户偏好和列车规则分配座位| AllocateSeats
        AllocateSeats -->|并发控制机制| ConcurrencyControl[并发控制]
    end

    subgraph 订单确认和支付
        ConfirmOrder -->|支付流程| PaymentProcess[支付流程]
        PaymentProcess -->|更新订单状态| UpdateOrderStatus
        UpdateOrderStatus -->|生成车票| GenerateTicket
        GenerateTicket -->|通知用户| NotifyUser
    end

    subgraph 批处理和作业调度
        ScheduleBatchJobs[调度批量作业] -->|不同类型的批量作业| BatchJobs[批量作业]
        BatchJobs -->|使用Quartz进行作业调度| Quartz[Quartz调度]
        Quartz -->|日志和监控| LoggingMonitoring[日志和监控]
    end
```

## 时序图

```mermaid
sequenceDiagram
    actor 用户
    participant 网关 as 网关
    participant 业务服务 as 业务服务
    participant 会员服务 as 会员服务
    participant 数据库 as 数据库

    用户->>网关: 发送注册请求
    网关->>业务服务: 转发注册请求
    业务服务->>数据库: 保存用户信息
    数据库-->>业务服务: 返回保存结果
    业务服务-->>网关: 返回注册结果
    网关-->>用户: 返回注册结果

    用户->>网关: 发送登录请求
    网关->>业务服务: 转发登录请求
    业务服务->>数据库: 验证用户信息
    数据库-->>业务服务: 返回验证结果
    业务服务-->>网关: 返回登录结果
    网关-->>用户: 返回登录结果

    用户->>网关: 发送购票请求
    网关->>业务服务: 转发购票请求
    业务服务->>数据库: 查询余票信息
    数据库-->>业务服务: 返回余票信息
    业务服务->>会员服务: 查询会员信息
    会员服务-->>业务服务: 返回会员信息
    业务服务->>数据库: 保存订单信息
    数据库-->>业务服务: 返回保存结果
    业务服务-->>网关: 返回购票结果
    网关-->>用户: 返回购票结果

    用户->>网关: 发送选座请求
    网关->>业务服务: 转发选座请求
    业务服务->>数据库: 查询座位信息
    数据库-->>业务服务: 返回座位信息
    业务服务->>数据库: 更新座位信息
    数据库-->>业务服务: 返回更新结果
    业务服务-->>网关: 返回选座结果
    网关-->>用户: 返回选座结果

    用户->>网关: 发送确认订单请求
    网关->>业务服务: 转发确认订单请求
    业务服务->>数据库: 查询订单信息
    数据库-->>业务服务: 返回订单信息
    业务服务->>数据库: 更新订单状态
    数据库-->>业务服务: 返回更新结果
    业务服务-->>网关: 返回确认订单结果
    网关-->>用户: 返回确认订单结果

    用户->>网关: 发送查询排队数量请求
    网关->>业务服务: 转发查询排队数量请求
    业务服务->>数据库: 查询排队数量
    数据库-->>业务服务: 返回排队数量
    业务服务-->>网关: 返回排队数量结果
    网关-->>用户: 返回排队数量结果
```

