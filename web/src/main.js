import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import Antd, {notification} from 'ant-design-vue';
import 'ant-design-vue/dist/antd.css';
import * as Icons from'@ant-design/icons-vue';
import axios from 'axios';
import './assets/js/enums'

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
  const _token = store.state.member.token;
  if (_token) {
    config.headers.token = _token;
    console.log('请求headers增加token：', _token);
  }
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
  const _response = error.response;
  const status = _response.status;
  if (status === 401) {
    // 判断状态码是401 跳转到登录页
    console.log("未登录或登录超时，跳到登录页");
    store.commit('setMember', {});
    notification.error({description: '登录过期，请重新登录！'});
    router.push('/login');
  }
  return Promise.reject(error);
});
axios.defaults.baseURL = process.env.VUE_APP_SERVER;

console.log('环境: ', process.env.NODE_ENV);
console.log('服务端: ', process.env.VUE_APP_SERVER);

