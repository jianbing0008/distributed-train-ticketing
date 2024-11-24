<template>
  <!--p防止与下面元素重叠 -->
  <p>
    <a-button type="primary" @click="showModal">新增</a-button>
  </p>

  <!--乘车人员展示-->
  <a-table :dataSource="passengers" :columns="columns" :pagination="pagination"/>
  <!-- 新增弹窗 -->
  <a-modal v-model:visible="visible" title="乘车人" @ok="handleOk"
           ok-text="确认" cancel-text="取消">
    <a-form
        :model="passenger"
        name="basic"
        :label-col="{ span: 4 }"
        :wrapper-col="{ span: 16 }"
        autocomplete="off"
        @finish="onFinish"
        @finishFailed="onFinishFailed"
    >
      <a-form-item label="姓名">
        <a-input v-model:value="passenger.name" />
      </a-form-item>

      <a-form-item label="身份证">
        <a-input v-model:value="passenger.idCard" />
      </a-form-item>

      <a-form-item label="类型">
        <a-select v-model:value="passenger.type" >
          <a-select-option value="1">成人</a-select-option>
          <a-select-option value="2">儿童</a-select-option>
          <a-select-option value="3">学生</a-select-option>
        </a-select>
      </a-form-item>

    </a-form>
  </a-modal>
</template>

<script>
//reactive数组重新赋值会失去响应式特性
import {defineComponent, ref, reactive, onMounted} from 'vue';
import axios from "axios";
import {notification} from "ant-design-vue";

export default defineComponent({
  setup() {

    const passenger = reactive({
      id: undefined,
      memberId: undefined,
      name: undefined,
      idCard: undefined,
      type: undefined,
      createTime: undefined,
      updateTime: undefined,
    });
    //声明ref可以直接赋值  reactive要使用value
    const passengers = ref([]);

    const pagination = reactive({
      total: 0,
      current: 1,
      pageSize: 2,
    });

    const columns = [
      {
        title: '姓名',
        dataIndex: 'name',
        key: 'name',
      },
      {
        title: '身份证',
        dataIndex: 'idCard',
        key: 'idCard',
      },
      {
        title: '类型',
        dataIndex: 'type',
        key: 'type',
      },
    ]

    const visible = ref(false);
    const showModal = () => {
      visible.value = true;
    };
    const handleOk = () => {
      axios.post("/member/passenger/save", passenger
      ).then(response =>{
        let data = response.data;
        if(data.success) {
          notification.success({description:"乘车人基本信息保存成功"})
          visible.value = false;
        }else {
          notification.error({description: data.message})
        }

      })
    };

    const handlerQuery = param => {
      axios.get("/member/passenger/query-list",{
        params:{
          page: param.page,
          size: param.size
        }
      }).then((response) => {
        let data = response.data;
        if(data.success) {
          passengers.value = data.content.list;
          pagination.total = data.content.total;
          console.log("1111",passengers)
        }else{
          notification.error({description: data.message});
        }
      })
    }

    onMounted(() => {
      handlerQuery({
        page: 1,
        size: 5
      })
    })

    return {
      visible,
      showModal,
      handleOk,
      passenger,
      passengers,
      columns,
      handlerQuery,
      pagination,

    };
  },
});

</script>

<style>

</style>