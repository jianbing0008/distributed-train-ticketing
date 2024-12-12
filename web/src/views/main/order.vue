<template>
<div>{{dailyTrainTicket}}</div>
</template>

<script >
import {defineComponent} from "vue";
export default defineComponent({
  name: "order-view",
  setup(){
    const dailyTrainTicket = SessionStorage.get(SESSION_ORDER) || {}
    console.log("下单车票信息" + dailyTrainTicket)
    return {
      dailyTrainTicket
    }
  }
})

</script>


<style scoped>

</style>