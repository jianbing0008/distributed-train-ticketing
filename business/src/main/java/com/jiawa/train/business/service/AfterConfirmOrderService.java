package com.jiawa.train.business.service;

import com.jiawa.train.business.domain.ConfirmOrder;
import com.jiawa.train.business.domain.DailyTrainSeat;
import com.jiawa.train.business.domain.DailyTrainTicket;
import com.jiawa.train.business.enums.ConfirmOrderStatusEnum;
import com.jiawa.train.business.feign.MemberFeign;
import com.jiawa.train.business.mapper.ConfirmOrderMapper;
import com.jiawa.train.business.mapper.DailyTrainSeatMapper;
import com.jiawa.train.business.mapper.cust.DailyTrainTicketMapperCust;
import com.jiawa.train.business.req.ConfirmOrderTicketReq;
import com.jiawa.train.common.context.LoginMemberContext;
import com.jiawa.train.common.req.MemberTicketReq;
import com.jiawa.train.common.resp.CommonResp;
import io.seata.core.context.RootContext;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Slf4j


public class AfterConfirmOrderService {

    @Autowired
    private DailyTrainSeatMapper dailyTrainSeatMapper;
    @Autowired
    private DailyTrainTicketMapperCust dailyTrainTicketMapperCust;
    @Autowired
    private MemberFeign memberFeign;
    @Autowired
    private ConfirmOrderMapper confirmOrderMapper;



    /**
     * 选中座位后事务处理：
     * 座位表修改售卖情况sell；
     * 余票详情表修改余票；
     * 为会员增加购票记录
     * 更新确认订单为成功
     * @param finalSeatList
     */
    //@Transactional
    @GlobalTransactional
    public void afterDoConfirm(DailyTrainTicket dailyTrainTicket,
                               List<DailyTrainSeat> finalSeatList,
                               List<ConfirmOrderTicketReq> tickets,
                               ConfirmOrder confirmOrder) {
        log.info("seata全局事务ID：{}", RootContext.getXID());
        for (int j = 0; j < finalSeatList.size(); j++) {
            DailyTrainSeat dailyTrainSeat = finalSeatList.get(j);
            DailyTrainSeat seatForUpdate = new DailyTrainSeat();
            seatForUpdate.setId(dailyTrainSeat.getId());
            seatForUpdate.setSell(dailyTrainSeat.getSell());
            seatForUpdate.setUpdateTime(new Date());
            dailyTrainSeatMapper.updateByPrimaryKeySelective(seatForUpdate);

            /**
             * 计算这个站卖出去后,影响了哪些站的余票库存
             *             参照2-3节如何保证不超卖、不少卖,还要能承受极?高的并发10:30左右
             *             影响的库存:本次选座之前没卖过票的,和本次购买的的区间有交集的区间
             *             假设10个站,本次买4~7站
             *             原售:0010000001
             *             购买:000011100
             *             新售:001011101
             *             影响:XXX111111X
             *             Integer startindex = 4;
             *             Integer endindex = 7;
             *             Integer minStartindex = startindex - 往前碰到的最后一个'1' +1
             *             Integer maxStartindex = endIndex - 1;
             *             Integer minEndIndex = startIndex + 1;
             *             Integer maxEndindex=endindex + 往后碰到的最后一个1
             */
            /**
             * 公式的推导
             * 关于 minStartindex 公式推导
             * 目的：确定受本次购票影响的库存起始站范围的最小索引值。这个值要考虑到本次购买区间起始站之前，那些之前没卖过票（原售状态为 0 ）但因本次购买与之产生关联从而影响库存的最靠前位置。
             * 推导过程：
             * 假设我们把各站的售票情况看作一个序列（用二进制表示，0 表示未售票，1 表示已售票）。当确定本次购买的起始站索引是 startindex 时，我们需要往 “前” 找，找到最后一个状态为 0 的站的位置。
             * 比如有这样一个原售情况示例（假设总共 10 个站）：0010000001，本次购买起始站 startindex = 4 ，从这个位置往前看，最后一个 0 所在位置是索引为 3 的站。
             * 意味着从这个 0 的位置（索引 3 对应的站）开始往后，虽然之前没卖票，但由于本次购买涉及到后续区间（从第 4 站开始买），所以它的库存情况会受到影响。所以用 startindex 减去往前碰到的最后一个 0 的索引差值，就得到了受影响区间起始站的最小可能索引，即 minStartindex = startindex - 往前碰到的最后一个0 。
             * 关于 maxStartindex 公式推导
             * 目的：确定受本次购票影响的库存起始站范围的最大索引值。
             * 推导过程：
             * 考虑本次购买区间是从 startindex 站到 endindex 站。因为购买操作会对购买区间内各站以及与之相关联的相邻站库存产生影响，对于起始站这一侧来说，最大影响到的起始站位置应该就是购买区间终点站的前一站，即 endIndex - 1 这个位置。
             * 例如购买区间是第 4 站到第 7 站（startindex = 4 ，endindex = 7 ），那么从起始站这边看，第 6 站（7 - 1 ）就是起始站这边影响范围能到的最大索引位置了，再往后就是终点站本身了，所以 maxStartindex = endIndex - 1 。
             * 关于 minEndIndex 公式推导
             * 目的：确定受本次购票影响的库存终点站范围的最小索引值。
             * 推导过程：
             * 同样基于本次购买区间是从 startindex 站到 endindex 站这个前提，对于终点站这一侧影响库存的最小位置，应该就是购买区间起始站的后一站，即 startIndex + 1 。
             * 比如购买区间从第 4 站到第 7 站，第 5 站（4 + 1 ）就是终点站这边受影响的最小索引位置，因为从这个站开始往后到终点站区间内各站的库存都会因本次购买而受到影响，所以 minEndIndex = startIndex + 1 。
             * 关于 maxEndindex 公式推导
             * 目的：确定受本次购票影响的库存终点站范围的最大索引值。这个值要考虑到本次购买区间终点站之后，那些之前没卖过票（原售状态为 0 ）但因本次购买与之产生关联从而影响库存的最靠后位置。
             * 推导过程：
             * 还是看各站售票情况构成的序列（用二进制表示），当明确本次购买的终点站索引是 endindex 时，要往 “后” 找，找到最后一个状态为 0 的站的位置。
             * 例如原售情况为 0010000001 ，本次购买终点站 endindex = 7 ，从这个位置往后看，最后一个 0 所在位置是索引为 9 的站。
             * 这意味着从购买区间终点站（第 7 站）到这个后续最后一个 0 对应的站（第 9 站）之间的库存情况，尽管之前没卖票，但由于本次购买操作涉及到前面的区间，所以也会受到影响。所以用 endindex 加上往后碰到的最后一个 0 的索引差值，就得到了受影响区间终点站的最大可能索引，即 maxEndindex = endindex + 往后碰到的最后一个0 。
             */

            Integer startIndex = dailyTrainTicket.getStartIndex();
            Integer endIndex = dailyTrainTicket.getEndIndex();
            char[] chars = seatForUpdate.getSell().toCharArray();
            Integer maxStartIndex = endIndex - 1;
            Integer minEndIndex = startIndex + 1;
            Integer minStartIndex = 0;
            for (int i = startIndex - 1; i >= 0; i--) {
                if (chars[i] == '1') {
                    minStartIndex = i + 1;
                    break;
                }
            }
            log.info("影响出发站区间:{}-{}", minStartIndex, maxStartIndex);
            Integer maxEndIndex = seatForUpdate.getSell().length();
            for (int i = endIndex; i < seatForUpdate.getSell().length(); i++) {
                if (chars[i] == '1') {
                    maxEndIndex = i;//因为我们的目的是找到这个已售票站作为终点边界，而不是它之前的站。
                    break;
                }
            }
            log.info("影响到达站区间:{}-{}", minEndIndex, maxEndIndex);
            dailyTrainTicketMapperCust.updateCountBySell(
                    dailyTrainSeat.getDate(),
                    dailyTrainSeat.getTrainCode(),
                    dailyTrainSeat.getSeatType(),
                    minStartIndex, maxStartIndex, minEndIndex, maxEndIndex);

            //调用会员服务接口，为会员增加一张车票
                MemberTicketReq memberTicketReq = new MemberTicketReq();
                memberTicketReq.setId(LoginMemberContext.getId());
                memberTicketReq.setMemberId(LoginMemberContext.getId());
                memberTicketReq.setPassengerId(tickets.get(j).getPassengerId());
                memberTicketReq.setPassengerName(tickets.get(j).getPassengerName());
                memberTicketReq.setDate(dailyTrainTicket.getDate());
                memberTicketReq.setTrainCode(dailyTrainSeat.getTrainCode());
                memberTicketReq.setCarriageIndex(dailyTrainSeat.getCarriageIndex());
                memberTicketReq.setRow(dailyTrainSeat.getRow());
                memberTicketReq.setCol(dailyTrainSeat.getCol());
                memberTicketReq.setStart(dailyTrainTicket.getStart());
                memberTicketReq.setStartTime(dailyTrainTicket.getStartTime());
                memberTicketReq.setEnd(dailyTrainTicket.getEnd());
                memberTicketReq.setEndTime(dailyTrainTicket.getEndTime());
                memberTicketReq.setSeatType(dailyTrainSeat.getSeatType());
                memberTicketReq.setCreateTime(dailyTrainTicket.getStartTime());
                memberTicketReq.setUpdateTime(dailyTrainTicket.getUpdateTime());
                CommonResp<Object> commonResp = memberFeign.save(memberTicketReq);
                log.info("调用member，返回：{}",commonResp);

            ConfirmOrder confirmOrderStatus = new ConfirmOrder();
            confirmOrderStatus.setId(confirmOrder.getId());
            confirmOrderStatus.setStatus(ConfirmOrderStatusEnum.SUCCESS.getCode());
            confirmOrderMapper.updateByPrimaryKeySelective(confirmOrderStatus);



        }




    }
}
