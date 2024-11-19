import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import Antd from 'ant-design-vue';
import 'ant-design-vue/dist/antd.css';
import * as Icons from'@ant-design/icons-vue';
import axios from 'axios';

const app = createApp(App)
app.use(Antd).use(store).use(router).mount('#app')

//全局使用图标
const icons= Icons;
for (const i in icons) {
  app.component(i, icons[i]);
}

/**
 * axios拦截器
 */
// 请求拦截器
axios.interceptors.request.use(function (config) {
  // 在请求发送之前做些什么
  console.log('请求参数：', config);
  return config;
}, error => {
  // 对请求错误做些什么
  return Promise.reject(error);
});
// 响应拦截器
axios.interceptors.response.use(function (response) {
  // 对响应数据做点什么
  console.log('返回结果：', response);
  return response;
}, error => {
  // 对响应错误做点什么
  console.log('返回错误：', error);
  return Promise.reject(error);
});

