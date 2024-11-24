<template>
  <!-- 防止与下面元素重叠 -->
  <p>
    <a-space>
      <a-button type="primary" @click="handlerQuery()">刷新</a-button>
      <a-button type="primary" @click="onAdd">新增</a-button>
    </a-space>
  </p>

  <!-- 乘车人员展示 -->
  <a-table :dataSource="passengers"
           :columns="columns"
           :pagination="pagination"
           @change="handleTableChange"
           :loading="loading">

    <!-- 增加编辑乘客按钮 -->
    <template #bodyCell="{ column, record }">
      <template v-if="column.dataIndex === 'operation'">
        <a-space>
          <a-popconfirm
              title="删除后不可恢复，确认删除？"
              @confirm="onDelete(record)"
              ok-text="确认" cancel-text="取消">
            <a style="color: red">删除</a>
          </a-popconfirm>
          <a @click="onEdit(record)">编辑</a>
        </a-space>
      </template>
    </template>

  </a-table>
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
// reactive数组重新赋值会失去响应式特性
import {defineComponent, ref, onMounted} from 'vue';
import axios from "axios";
import {notification} from "ant-design-vue";

export default defineComponent({
  setup() {

    // 定义乘客信息的ref对象
    let passenger = ref({
      id: undefined,
      memberId: undefined,
      name: undefined,
      idCard: undefined,
      type: undefined,
      createTime: undefined,
      updateTime: undefined,
    });
    // 声明ref可以直接赋值  reactive要使用value
    const passengers = ref([]);

    // 定义分页信息的ref对象
    const pagination = ref({
      total: 0,
      current: 1,
      pageSize: 2,
    });

    let loading = ref(false); // 如果是ture的话就表示加载中

    // 定义表格列配置
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
      {
        title: '操作',
        dataIndex: 'operation',
      },
    ]

    const visible = ref(false);

    // 打开新增弹窗的方法
    const onAdd = () => {
      //新增表单同时会同步前端的表单的数据，所以每次点击新增表单时清空passenger
      passenger.value = {};
      visible.value = true;
    };

    // 编辑乘客信息的方法
    const onEdit = (record) => {
      // 深拷贝,这样修改不会影响原来的数据，不然前端页面显示的数据会跟着edit框用户输入的数据一起变
      passenger.value = window.Tool.copy(record);
      visible.value = true;
    };

    // 删除乘客信息的方法
    const onDelete = (record) => {
      axios.delete("/member/passenger/delete/" + record.id).then(response => {
        let data = response.data;
        if (data.success) {
          notification.success({description: "删除成功"});
          handlerQuery({ // 删除成功后刷新数据
            page: pagination.value.current,
            size: pagination.value.pageSize
          });
        } else {
          notification.error({description: data.message})
        }
      })
    };

    // 保存乘客信息的方法
    const handleOk = () => {
      axios.post("/member/passenger/save", passenger.value
      ).then(response => {
        let data = response.data;
        if (data.success) {
          notification.success({description: "乘车人基本信息保存成功"})
          visible.value = false;
          handlerQuery({ // 保存成功后刷新数据
            page: pagination.value.current,
            size: pagination.value.pageSize
          });
        } else {
          notification.error({description: data.message})
        }

      })
    };

    // 查询乘客信息的方法
    const handlerQuery = param => {
      if (!param) {
        param = {
          page: 1,
          size: pagination.value.pageSize
        };
      }
      loading.value = true; // 加载中，
      axios.get("/member/passenger/query-list", {
        params: {
          page: param.page,
          size: param.size
        }
      }).then((response) => {
        loading.value = false;
        let data = response.data;
        if (data.success) {
          passengers.value = data.content.list;
          // 设置分页控件的值
          pagination.value.current = param.page;
          pagination.value.total = data.content.total;
          console.log("1111", passengers)
        } else {
          notification.error({description: data.message});
        }
      })
    }

    // 表格分页变化的处理方法
    const handleTableChange = (pagination) => {
      handlerQuery({
        page: pagination.current,
        size: pagination.pageSize
      });
    };

    // 组件挂载时查询乘客信息
    onMounted(() => {
      handlerQuery({
        page: 1,
        size: pagination.value.pageSize
      })
    })

    return {
      visible,
      onAdd,
      onEdit,
      onDelete,
      handleOk,
      passenger,
      passengers,
      columns,
      handlerQuery,
      pagination,
      handleTableChange,
      loading,

    };
  },
});

</script>

<style>

</style>
