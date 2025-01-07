package com.jiawa.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jiawa.train.business.domain.*;
import com.jiawa.train.business.dto.ConfirmOrderMQDto;
import com.jiawa.train.business.enums.ConfirmOrderStatusEnum;
import com.jiawa.train.business.enums.RedisKeyPreEnum;
import com.jiawa.train.business.enums.SeatColEnum;
import com.jiawa.train.business.enums.SeatTypeEnum;
import com.jiawa.train.business.mapper.ConfirmOrderMapper;
import com.jiawa.train.business.req.ConfirmOrderDoReq;
import com.jiawa.train.business.req.ConfirmOrderQueryReq;
import com.jiawa.train.business.req.ConfirmOrderTicketReq;
import com.jiawa.train.business.resp.ConfirmOrderQueryResp;
import com.jiawa.train.common.exception.BusinessException;
import com.jiawa.train.common.exception.BusinessExceptionEnum;
import com.jiawa.train.common.resp.PageResp;
import com.jiawa.train.common.util.SnowUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
/**
 * ConfirmOrder服务类，负责处理与ConfirmOrder相关的业务逻辑
 */
public class ConfirmOrderService {

    @Autowired
    private ConfirmOrderMapper confirmOrderMapper;
    @Autowired
    private DailyTrainTicketService dailyTrainTicketService;
    @Autowired
    private DailyTrainCarriageService dailyTrainCarriageService;
    @Autowired
    private DailyTrainSeatService dailyTrainSeatService;
    @Autowired
    private AfterConfirmOrderService afterConfirmOrderService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private SkTokenService skTokenService;

    /**
     *  查询前面有几个人在排队
     */

    public Integer queryLineCount(Long id) {
        ConfirmOrder confirmOrder = confirmOrderMapper.selectByPrimaryKey(id);
        // 根据订单状态获取枚举
        ConfirmOrderStatusEnum statusEnum = EnumUtil.getBy(ConfirmOrderStatusEnum::getCode, confirmOrder.getStatus());
        int result = switch (statusEnum) {
            case PENDING -> 0;   //排队 0
            case SUCCESS -> -1;   //成功
            case FAILURE -> -2;   //失败
            case EMPTY -> -3;   //无票
            case CANCEL -> -4;   //排队 0
            case INIT -> 999;   // 需要查表得到实际排队数量
        };
        if(result == 999) {
            // 排在第几位 where a = 1 and (b=1 or c=1) 等价于 where a = 1 and b=1 or a=1 and c=1
            ConfirmOrderExample confirmOrderExample = new ConfirmOrderExample();
            confirmOrderExample.or().andTrainCodeEqualTo(confirmOrder.getTrainCode())
                    .andDateEqualTo(confirmOrder.getDate())
                    .andCreateTimeLessThan(confirmOrder.getCreateTime())
                    .andStatusEqualTo(ConfirmOrderStatusEnum.PENDING.getCode());
            confirmOrderExample.or().andTrainCodeEqualTo(confirmOrder.getTrainCode())
                    .andDateEqualTo(confirmOrder.getDate())
                    .andCreateTimeLessThan(confirmOrder.getCreateTime())
                    .andStatusEqualTo(ConfirmOrderStatusEnum.INIT.getCode());
            return Math.toIntExact(confirmOrderMapper.countByExample(confirmOrderExample));
        }else {
            return result;
        }
    }

    /**
     * 保存ConfirmOrder信息
     *
     * @param req ConfirmOrder保存请求对象，包含ConfirmOrder的基本信息
     */
    public void save(ConfirmOrderDoReq req){
        // 获取当前时间，用于记录ConfirmOrder信息的创建和更新时间
        DateTime now = DateTime.now();
        // 将请求对象转换为ConfirmOrder对象，便于后续操作
        ConfirmOrder confirmOrder = BeanUtil.copyProperties(req, ConfirmOrder.class);
        if(ObjectUtil.isNull(req.getTickets())){ // 判断是否为空，为空则是新增ConfirmOrder
            // 设置ConfirmOrder的会员ID，来源于登录会员上下文
            // 生成ConfirmOrder的唯一ID
            confirmOrder.setId(SnowUtil.getSnowflakeNextId());
            // 设置ConfirmOrder信息的创建和更新时间为当前时间
            confirmOrder.setCreateTime(now);
            confirmOrder.setUpdateTime(now);
            // 插入ConfirmOrder信息到数据库
            confirmOrderMapper.insert(confirmOrder);
        }else{  // 不为空则更新ConfirmOrder信息
            confirmOrder.setUpdateTime(now);
            confirmOrderMapper.updateByPrimaryKey(confirmOrder);
        }

    }

    /**
     * 查询ConfirmOrder列表
     *
     * @param req ConfirmOrder查询请求对象，可能包含ConfirmOrder的会员ID等查询条件
     */
    public PageResp<ConfirmOrderQueryResp> queryList(ConfirmOrderQueryReq req){
        // 创建ConfirmOrder示例对象，用于构造查询条件
        ConfirmOrderExample confirmOrderExample = new ConfirmOrderExample();
        //根据id倒序排序
        confirmOrderExample.setOrderByClause("id desc");
        // 创建查询条件对象
        ConfirmOrderExample.Criteria criteria = confirmOrderExample.createCriteria();


        // 记录查询日志
        log.info("查询页码：{}", req.getPage());
        log.info("每页条数：{}", req.getSize());
        // 启用分页查询
        PageHelper.startPage(req.getPage(),req.getSize());
        // 根据构造的查询条件，从数据库中选择符合条件的ConfirmOrder信息
        List<ConfirmOrder> confirmOrderList = confirmOrderMapper.selectByExample(confirmOrderExample);

        // 创建PageInfo对象，用于获取分页信息
        PageInfo<ConfirmOrder> pageInfo = new PageInfo<>(confirmOrderList);
        // 记录分页信息日志
        log.info("总行数：{}", pageInfo.getTotal());
        log.info("总页数：{}", pageInfo.getPages());

        // 将查询结果列表转换为目标响应对象列表
        List<ConfirmOrderQueryResp> list = BeanUtil.copyToList(confirmOrderList, ConfirmOrderQueryResp.class);

        // 创建并组装分页响应对象
        PageResp<ConfirmOrderQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    public void delete(Long id) {
        confirmOrderMapper.deleteByPrimaryKey(id);
    }

//        @SentinelResource("doConfirm")
    @SentinelResource(value = "doConfirm", blockHandler = "doConfirmBlock")
    public void doConfirm(ConfirmOrderMQDto confirmOrderMQDto){

        String lockKey = RedisKeyPreEnum.CONFIRM_ORDER + "-" + DateUtil.formatDate(confirmOrderMQDto.getDate()) + "-" + confirmOrderMQDto.getTrainCode();
        RLock lock = null;
        try {
            // 使用redisson，自带看门狗
            lock = redissonClient.getLock(lockKey);
            boolean tryLock = lock.tryLock(0, TimeUnit.SECONDS); // 带看门狗
            if (tryLock) {
                log.info("恭喜，抢到锁了！");
            } else {
                // 只是没抢到锁，并不知道票抢完了没，所以提示稍候再试
                log.info("很遗憾，没抢到锁,有其他消费线程正在处理订单");
                return;
//                throw new BusinessException(BusinessExceptionEnum.BUSINESS_CONFIRM_LOCK_ERROR);
            }
            // 由先抢到分布式锁的消费者处理所有同期车次初始化订单
            while (true) {
                // 取确认订单表的记录，同期车次，状态是I，分页处理(后端处理大量数据时，使用分页，避免一次性读到内存上)
                ConfirmOrderExample confirmOrderExample = new ConfirmOrderExample();
                ConfirmOrderExample.Criteria criteria = confirmOrderExample.createCriteria();
                criteria.andTrainCodeEqualTo(confirmOrderMQDto.getTrainCode())
                        .andDateEqualTo(confirmOrderMQDto.getDate())
                        .andStatusEqualTo(ConfirmOrderStatusEnum.INIT.getCode());
                PageHelper.startPage(1,5);
                List<ConfirmOrder> list = confirmOrderMapper.selectByExampleWithBLOBs(confirmOrderExample);

                if(CollUtil.isEmpty(list)) {
                    log.info("没有需要处理的订单，结束循环");
                    break;
                }else{
                    log.info("本次处理{}条订单",list.size());
                }
                // 一条一条卖
                list.forEach(confirmOrder -> {
                    try {
                        sell(confirmOrder);
                    } catch (BusinessException e) {
                        if(e.equals(BusinessExceptionEnum.CONFIRM_ORDER_TICKET_COUNT_ERROR)) {// 余票不足
                            // 这里处理失败下一订单，是因为当前订单没票，不代表同一车次不同站没票
                            log.info("本订单余票不足，继续售卖下一个订单");
                            confirmOrder.setStatus(ConfirmOrderStatusEnum.FAILURE.getCode());
                        }else{
                            throw e;
                        }
                    }
                });
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //注意：finally又将释放锁加上了，因为看门狗的isHeldByCurrentThread()可以判断是否是当前线程的锁再去释放
            log.info("购票流程结束，释放锁！");
            if (null != lock && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private void sell(ConfirmOrder confirmOrder) {
        // 省略业务数据校验，如：车次是否存在，余票是否存在，车次是否在有效期内，tickets条数>0，同乘客同车次是否已买过
        ConfirmOrderDoReq confirmOrderDoReq = new ConfirmOrderDoReq();
        confirmOrderDoReq.setMemberId(confirmOrder.getMemberId());
        confirmOrderDoReq.setDate(confirmOrder.getDate());
        confirmOrderDoReq.setTrainCode(confirmOrder.getTrainCode());
        confirmOrderDoReq.setStart(confirmOrder.getStart());
        confirmOrderDoReq.setEnd(confirmOrder.getEnd());
        confirmOrderDoReq.setDailyTrainTicketId(confirmOrderDoReq.getDailyTrainTicketId());
        confirmOrderDoReq.setTickets(JSON.parseArray(confirmOrder.getTickets(),ConfirmOrderTicketReq.class));
        confirmOrderDoReq.setImageCode("");
        confirmOrderDoReq.setLogId("");
        confirmOrderDoReq.setImageCodeToken("");

        // 将订单设置成处理中，避免重复处理
        log.info("将确认订单更新成处理中，避免重复处理,confirm_order:{}",confirmOrder.getStatus());
        confirmOrder.setStatus(ConfirmOrderStatusEnum.PENDING.getCode());
        updateStatus(confirmOrder);

        // 查出余票记录，需要得到真实的库存
        DailyTrainTicket dailyTrainTicket = dailyTrainTicketService.selectByUnique(confirmOrder.getDate(), confirmOrder.getTrainCode(), confirmOrder.getStart(), confirmOrder.getEnd());
        log.info("查出余票信息：{}", dailyTrainTicket);

        // 扣减余票数量，并判断余票是否足够
        reduceTickets(confirmOrderDoReq, dailyTrainTicket);

        //最终的选座结果
        List<DailyTrainSeat> finalSeatList = new ArrayList<>();

        //计算相对第一个座位的偏离值
        //比如选择的是C1,D2,则偏移值是:[0,5]
        //比如读择的是A1,B1,C1,则偏移值是:[0,1,2]
        ConfirmOrderTicketReq ticketReq0 = confirmOrderDoReq.getTickets().get(0);
        if(StrUtil.isNotBlank(ticketReq0.getSeat())){
            log.info("本次购票有选座，第一张票的座号：{}", ticketReq0.getSeat());
            //查出本次选座的座位类型都有哪些列，用于计算所选座位和第一个座位的偏移值
            List<SeatColEnum> colEnumList = SeatColEnum.getColsByType(ticketReq0.getSeatTypeCode());
            log.info("本次选座的座位类型包含的列有：{}", colEnumList);

            //组成和前端两排选座一样的列表，用于做参照的座位列表，如：referSeatList = {A1,C1,D1,F1,A2,C2,D2,F2}
            List<String> referSeatList = new ArrayList<>();
            for(int i = 1; i <=2 ; i++){
                for(SeatColEnum seatColEnum: colEnumList){
                    referSeatList.add(seatColEnum.getCode() + (i));
                }
            }
            log.info("参照座位列表：{}", referSeatList);
            List<Integer> offsetList = new ArrayList<>();
            //绝对偏移值,即:在参照座位列表中的位置
            List<Integer> absoluteOffsetList = new ArrayList<>();
            for(ConfirmOrderTicketReq ticketReq: confirmOrderDoReq.getTickets()){
                int index = referSeatList.indexOf(ticketReq.getSeat());
                absoluteOffsetList.add(index);
            }
            log.info("所有座位的绝对偏移值：{}", absoluteOffsetList);
            //相对偏移值,即：第一个座位和当前座位的偏移值
            for(Integer index: absoluteOffsetList){
                int offset = index - absoluteOffsetList.get(0);
                offsetList.add(offset);
            }
            log.info("所有座位的相对偏移值：{}", offsetList);

            getSeat(finalSeatList,
                    confirmOrderDoReq.getDate(),
                    confirmOrderDoReq.getTrainCode(),
                    ticketReq0.getSeatTypeCode(), ticketReq0.getSeat().split("")[0],//例如：A1，分成【A，1】，取第0号元素
                    offsetList,
                    dailyTrainTicket.getStartIndex(),
                    dailyTrainTicket.getEndIndex()
            );
        }else{
            log.info("本次购票没有选座");
            for(ConfirmOrderTicketReq ticketReq: confirmOrderDoReq.getTickets()){
                getSeat(finalSeatList,
                        confirmOrderDoReq.getDate(),
                        confirmOrderDoReq.getTrainCode(),
                        ticketReq.getSeatTypeCode(), //可能第一张票买的一等座，第二张票买的二等，所以要拿到当前车票的座位类型
                        null,
                        null,
                        dailyTrainTicket.getStartIndex(),
                        dailyTrainTicket.getEndIndex()
                );
            }
        }
        log.info("最终的选座结果：{}", finalSeatList);

        // 选中座位后事务处理：
        // 座位表修改售卖情况sell；
        // 余票详情表修改余票；
        // 为会员增加购票记录
        // 更新确认订单为成功
        try{
            afterConfirmOrderService.afterDoConfirm(dailyTrainTicket,finalSeatList, confirmOrderDoReq.getTickets(),confirmOrder);
        }catch (Exception e){
            log.error("保存购票信息失败 ", e);
            throw new BusinessException(BusinessExceptionEnum.BUSINESS_CONFIRM_ORDER_ERROR);
        }
    }

    /**
     * 更新订单状态
     * @param confirmOrder
     */
    private void updateStatus(ConfirmOrder confirmOrder) {
        ConfirmOrder confirmOrderForUpdate = new ConfirmOrder();
        confirmOrderForUpdate.setId(confirmOrder.getId());
        confirmOrderForUpdate.setUpdateTime(new Date());
        confirmOrderForUpdate.setStatus(confirmOrder.getStatus());
        confirmOrderMapper.updateByPrimaryKeySelective(confirmOrderForUpdate);
    }

    /**
     * 选座, 根据前端传来的座位类型和座位号，从余票详情表中获取座位信息
     * 如果有选座则一次性挑完，如果没选座，则一个一个挑
     * @param date
     * @param trainCode
     * @param seatType
     * @param column
     * @param offsetList
     */
    private void getSeat(List<DailyTrainSeat> finalSeatList, Date date, String trainCode, String seatType,String column,
                         List<Integer> offsetList,
                         Integer startIndex, Integer endIndex){

        List<DailyTrainSeat> getSeatList = new ArrayList<>();

        List<DailyTrainCarriage> carriageList = dailyTrainCarriageService.selectBySeatType(date, trainCode, seatType);
        log.info("共查出{}个符合条件的车厢,车箱列表：{}", carriageList.size(), carriageList);

        // 一个车箱一个车箱的获取座位数据
        for(DailyTrainCarriage dailyTrainCarriage: carriageList){
            log.info("开始从车箱[{}]选座", dailyTrainCarriage.getIndex());
            getSeatList = new ArrayList<>(); //换车厢的时候，也应该清空临时座位列表
            List<DailyTrainSeat> seatList = dailyTrainSeatService.selectByCarriage(date, trainCode, dailyTrainCarriage.getIndex());
            log.info("车箱[{}]的座位数：{}", dailyTrainCarriage.getIndex(), seatList.size());
            for (int i = 0; i < seatList.size(); i++) {
                DailyTrainSeat dailyTrainSeat = seatList.get(i);
                String col = dailyTrainSeat.getCol();
                Integer seatIndex = dailyTrainSeat.getCarriageSeatIndex();

                //判断当前座位不能被选中过
                boolean alreadyChooseFlag = false;
                for (DailyTrainSeat finalSeat : finalSeatList) {
                    if(finalSeat.getId().equals(dailyTrainSeat.getId())){
                        alreadyChooseFlag = true;
                        break;
                    }
                }
                if (alreadyChooseFlag) {
                    log.info("座位{}被选中过，不能重复选中，继续判断下一个座位", seatIndex);
                    continue; //当前会员已经选过该座位，跳过
                }

                //判断column，非空的话比对列号，空的话代表没有选座
                if (StrUtil.isBlank(column)) {
                    log.info("无选座");
                } else {
                    if (!col.equals(column)) {
                        log.info("座位{}的列号不匹配，当前列号：{}，选座列号：{}", seatIndex, col, column);
                        continue;//继续处理seatList的下一个元素
                    }
                }


                boolean isChoose = calSell(dailyTrainSeat, startIndex, endIndex);
                if (isChoose) {
                    log.info("选中座位");
                    getSeatList.add(dailyTrainSeat);
                } else {
                    continue;
                }

                //根据offset去选剩下的座位
                boolean isGetAllOffsetSeat = true;
                if (CollUtil.isNotEmpty(offsetList)) {
                    log.info("有偏移值:{}, 校验偏移的座位是否可选", offsetList);
                    //从索引1开始，因为索引0就是当前已选中的票
                    for (int j = 1; j < offsetList.size(); j++) {
                        Integer offset = offsetList.get(j);
                        //座位的索引在数据库中是从 1 开始的
//                        int nextIndex = seatIndex + offset - 1;
                        int nextIndex = i + offset;

                        //有选座时，一定是在同一个车厢
                        if (nextIndex < 0 || nextIndex >= seatList.size()) {
                            log.info("座位{}超出座位列表长度，座位列表长度为：{}，跳过", nextIndex, seatList.size());
                            isGetAllOffsetSeat = false;
                            break;
                        }

                        DailyTrainSeat nextDailyTrainSeat = seatList.get(nextIndex);
                        boolean isChooseNext = calSell(nextDailyTrainSeat, startIndex, endIndex);
                        if (isChooseNext) {
                            log.info("座位{}被选中", nextDailyTrainSeat.getCarriageSeatIndex());
                            getSeatList.add(nextDailyTrainSeat);
                        } else {
                            log.info("座位{}不可选", nextDailyTrainSeat.getCarriageSeatIndex());
                            isGetAllOffsetSeat = false;
                            break;
                        }
                    }
                }
                if (!isGetAllOffsetSeat) {
                    getSeatList = new ArrayList<>();//清空临时座位列表
                    continue;
                }

                //保存选好的座位
                finalSeatList.addAll(getSeatList);
                return;
            }
        }

    }

    /**
     * 计算某座位在区间内是否有余票
     * 例:sell=10001.本次购买区间站1~4,看到区间已售是000，则可以购买这段区间的票
     * 全部是0,表示这个区间可买;只要有1,就表示区间内已售过票
     *
     * 选中后,要计算购票后的sell,比如原来是10001,本次购买区间站1~4
     * 方案:构造本次购票造成的售卖信息1110,和原sell 10001按位或,最终得到111111
     */
    private boolean calSell(DailyTrainSeat dailyTrainSeat, Integer startIndex, Integer endIndex){
        String sell = dailyTrainSeat.getSell();//10001

        String sellPart = sell.substring(startIndex, endIndex);//000
        if(Integer.parseInt(sellPart) > 0){
            log.info("座位[{}]区间[{}-{}]的票已售，无法购买", dailyTrainSeat.getCarriageSeatIndex(), startIndex, endIndex);
            return false;
        }else{
            log.info("座位[{}]区间[{}-{}]的票未售出，可以购买", dailyTrainSeat.getCarriageSeatIndex(), startIndex, endIndex);
            //  111
            String curSell = sellPart.replace('0', '1');
            // 0111
            curSell = StrUtil.fillBefore(curSell, '0', endIndex);
            // 01110
            curSell = StrUtil.fillAfter(curSell, '0', sell.length());

            //当前区间售票信息与库里的已售信息按位与,即可得到该座位卖出此票后的售票详情
            int newSellInt = NumberUtil.binaryToInt(curSell) | NumberUtil.binaryToInt(sell);//31
            String newSell = NumberUtil.getBinaryStr(newSellInt);//11111
            newSell = StrUtil.fillBefore(newSell, '0', sell.length());
            log.info("座位{}被选中，原售票信息：{}，车站区间：{}~{}，即：{}，最终售票信息：{}",
                    dailyTrainSeat.getCarriageSeatIndex(), sell, startIndex, endIndex, curSell, newSell);
            dailyTrainSeat.setSell(newSell);
        }
        return true;
    }

    /**
     * 扣减余票数量，并判断余票是否足够
     * @param req
     * @param dailyTrainTicket
     */
    private static void reduceTickets(ConfirmOrderDoReq req, DailyTrainTicket dailyTrainTicket) {
        for(ConfirmOrderTicketReq ticketReq: req.getTickets()){
            String seatTypeCode = ticketReq.getSeatTypeCode();
            SeatTypeEnum seatTypeEnum = EnumUtil.getBy(SeatTypeEnum::getCode, seatTypeCode);
            switch(seatTypeEnum){
                case YDZ ->{
                    int countLeft = dailyTrainTicket.getYdz() - 1;
                    if (countLeft < 0){
                        throw new BusinessException(BusinessExceptionEnum.BUSINESS_CONFIRM_ORDER_TICKET_NUM_ERROR);
                    }
                    dailyTrainTicket.setYdz(countLeft);
                }
                case EDZ ->{
                    int countLeft = dailyTrainTicket.getEdz() - 1;
                    if (countLeft < 0){
                        throw new BusinessException(BusinessExceptionEnum.BUSINESS_CONFIRM_ORDER_TICKET_NUM_ERROR);
                    }
                    dailyTrainTicket.setEdz(countLeft);
                }
                case RW ->{
                    int countLeft = dailyTrainTicket.getRw() - 1;
                    if (countLeft < 0){
                        throw new BusinessException(BusinessExceptionEnum.BUSINESS_CONFIRM_ORDER_TICKET_NUM_ERROR);
                    }
                    dailyTrainTicket.setRw(countLeft);
                }
                case YW ->{
                    int countLeft = dailyTrainTicket.getYw() - 1;
                    if (countLeft < 0){
                        throw new BusinessException(BusinessExceptionEnum.BUSINESS_CONFIRM_ORDER_TICKET_NUM_ERROR);
                    }
                    dailyTrainTicket.setYw(countLeft);
                }
            }
        }
    }

    /**
     * 降级方法，需包含原方法的所有参数和BlockException参数
     * @param req
     * @param e
     */
    public void doConfirmBlock(ConfirmOrderDoReq req, BlockException e) {
        log.info("购票请求被限流：{}", req);
        throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_FLOW_EXCEPTION);
    }


}
