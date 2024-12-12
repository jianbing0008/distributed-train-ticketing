<template>
  <div class="order-train">
    <span class="order-train-main">{{dailyTrainTicket.date}}</span>&nbsp;
    <span class="order-train-main">{{dailyTrainTicket.trainCode}}</span>次&nbsp;
    <span class="order-train-main">{{dailyTrainTicket.start}}</span>站
    <span class="order-train-main">({{dailyTrainTicket.startTime}})</span>&nbsp;
    <span class="order-train-main">——</span>&nbsp;
    <span class="order-train-main">{{dailyTrainTicket.end}}</span>站
    <span class="order-train-main">({{dailyTrainTicket.endTime}})</span>&nbsp;

    <div class="order-train-ticket">
      <span v-for="item in seatTypes" :key="item.type">
        <span>{{item.desc}}</span>
        <span class="order-train-ticket-main">{{item.price}}￥</span>&nbsp;
        <span class="order-train-ticket-main">{{item.count}}</span>&nbsp;张票&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
      </span>

    </div>
  </div>
  <a-divider></a-divider>
  <b>勾选要购票的乘客：</b>&nbsp;
  <a-checkbox-group v-model:value="passengerCheck" :options="passengerOption"/>
  <br/>
  选中的乘客：{{passengerCheck}}
  <br/>
  购票列表：{{tickets}}
</template>

<script >
import {defineComponent, onMounted, ref, watch} from "vue";
import axios from "axios";
import {notification} from "ant-design-vue";
export default defineComponent({
  name: "order-view",
  setup(){
    const passengers = ref([])
    const passengerOption = ref([]) //列表展示
    const passengerCheck = ref([]) //勾选的值
    const dailyTrainTicket = SessionStorage.get(SESSION_ORDER) || {}
    console.log("下单车票信息" + dailyTrainTicket)

    const SEAT_TYPE = window.SEAT_TYPE;
    console.log(SEAT_TYPE)
    // 本车次提供的座位类型seatTypes，含票价，余票等信息，例：
    // {
    //   type: "YDZ",
    //   code: "1",
    //   desc: "一等座",
    //   count: "100",
    //   price: "50",
    // }
    // 关于SEAT_TYPE[KEY]：当知道某个具体的属性xxx时，可以用obj.xxx，当属性名是个变量时，可以使用obj[xxx]
    const seatTypes = [];
    for (let KEY in SEAT_TYPE) {
      let key = KEY.toLowerCase();
      if (dailyTrainTicket[key] >= 0) {
        //余票数
        // console.log("dailyTrainTicket"+  JSON.stringify(dailyTrainTicket))
        // console.log("dailyTrainTicket[key]"+dailyTrainTicket[key])
        seatTypes.push({
          type: KEY,
          code: SEAT_TYPE[KEY]["code"],
          desc: SEAT_TYPE[KEY]["desc"],
          count: dailyTrainTicket[key],
          price: dailyTrainTicket[key + 'Price'],
        })
      }
    }
    console.log("本车次提供的座位：", seatTypes)

    // 购票列表,用于界面展示，并传递到后端接口，用来描述：哪位乘客购买什么座位的票
    //{
    //  passengerId: 123,
    //  passengerIdCard: "123456789",
    //  passengerName: "张三",
    //  passengerType: "1",
    //  seatTypeCode: "1",
    //}
    const tickets = ref([]);

    // 勾选或去掉某个乘客时，在购票列表中加上或去掉一张表
    watch(() => passengerCheck.value, (newVal, oldVal)=>{
      console.log("勾选乘客发生变化", newVal, oldVal)
      // 每次有变化时，把购票列表清空，重新构造列表
      tickets.value = [];
      passengerCheck.value.forEach((item) => tickets.value.push({
        passengerId: item.id,
        passengerType: item.type,
        seatTypeCode: seatTypes[0].code,
        passengerName: item.name,
        passengerIdCard: item.idCard
      }))
    }, {immediate: true});


    const handQueryPassenger = () => {
      axios.get("/member/passenger//query-mine").then(response => {
        let data = response.data;
        if (data.success) {
          passengers.value=data.content;
          passengers.value.forEach((item) => passengerOption.value.push(
              {
                label: item.name,//在列表中的显示
                value: item,//选中后返回给后端的参数  //现在绑定的值是一个对象，也就是一条条的乘客记录
              }
          ))
        } else {
          notification.error({description: data.message});
        }
      });
    };


    onMounted(() =>{
      handQueryPassenger()
    })

    return {
      dailyTrainTicket,
      seatTypes,
      handQueryPassenger,
      passengers,
      passengerOption,
      passengerCheck,
      tickets,
    }
  }
})

</script>


<style scoped>
.order-train .order-train-main {
  font-size: 18px;
  font-weight: bold;
}

.order-train .order-train-ticket {
  margin-top: 15px;
}
.order-train .order-train-ticket .order-train-ticket-main {
  color: red;
  font-size: 18px;
}
</style>