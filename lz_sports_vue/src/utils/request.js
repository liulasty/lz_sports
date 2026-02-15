import axios from "axios";

const http = axios.create({
  baseURL: 'http://localhost:80/sports',
  timeout: 10000,

})


// 添加请求拦截器
http.interceptors.request.use(function (config) {
  // 在发送请求之前做些什么
  // 获取JWT令牌
  const token = localStorage.getItem('jwtToken');
  // console.log("token", token)
  // 在请求头中添加自定义内容
  config.headers['jwt'] = token;


  return config;
}, function (error) {
  // 对请求错误做些什么
  return Promise.reject(error);
});

// 添加响应拦截器
http.interceptors.response.use(function (response) {
  // 2xx 范围内的状态码都会触发该函数。
  // 对响应数据做点什么

  if (response.data.message === '令牌校验失败') {
    ;
    console.log("删除JWT令牌", response)
    // 删除JWT令牌
    localStorage.removeItem('jwtToken');
    localStorage.removeItem('sportsUser');
  }
  return response;
}, function (error) {
  // 超出 2xx 范围的状态码都会触发该函数。
  console.log("网址", window.location.href)
  // 对响应错误做点什么
  // window.location.href = '/login'; 
  return Promise.reject(error);
});


export default http
