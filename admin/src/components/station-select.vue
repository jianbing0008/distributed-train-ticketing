<template>
  <a-select v-model:value="name" show-search allow-clear
            :filter-option="filterNameOption" placeholder="请选择车站"
            @change="onChange" :style="'width: ' + localWidth">

    <a-select-option v-for="item in stations" :key="item.name"
                     :value="item.name" :label="item.name + item.namePinyin + item.namePy">
      {{item.name}} | {{item.namePinyin}} ~ {{item.namePy}}
    </a-select-option>

  </a-select>
</template>

<script>
import {defineComponent, onMounted, ref, watch} from 'vue';
import axios from "axios";
import {notification} from "ant-design-vue";

export default defineComponent({
  name: "station-select-view",
  props:["modelValue", "width"],
  emits:['update:modelValue', 'change'],
  setup(props, {emit}) {
    const name = ref();
    const stations = ref([]);

    const localWidth = ref(props.width);
    if(Tool.isEmpty(props.width)) {
      localWidth.value = "100%";
    }

    //利用watch，动态获取父组件的值，如果放在onMounted或其他方法只会执行一次
    watch(() => props.modelValue, ()=>{
      console.log("props.modelValue",props.modelValue)
      name.value = props.modelValue;
    },{immediate: true})



    const queryAllTrain = () =>{
      axios.get("/business/admin/station/query-all").then((response) => {
        let data = response.data;
        if (data.success) {
          stations.value = data.content
        } else {
          notification.error({description: data.message});
        }
      })
    };
    /**
     * 车位下拉框筛选
     */
    const filterNameOption = (input, option) => {
      console.log(input,option);
      return option.label.toLowerCase().indexOf(input.toLowerCase()) >= 0;
    }

    /**
     * 将当前组件的值响应给父组件
     * @param value
     */
    const onChange = (value) => {
      emit('update:modelValue', value);
      let station = stations.value.filter(item => item.code === value)[0];
      if (Tool.isEmpty(station)) {
        station = {};
      }
      emit('change', station);
    };

    onMounted(() => {
      queryAllTrain();
    })
    return {
      name,
      stations,
      filterNameOption,
      onChange,
      localWidth,
      queryAllTrain,
    };
  },
});
</script>

<style>

</style>