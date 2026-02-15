import Vue from 'vue';
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';
import App from './App.vue';
import router from './router';
import store from './store';
import './api/mock';




//用来关闭 Vue 在启动时的生产提示
Vue.config.productionTip = false
//全局注册 ElementUI 组件库
Vue.use(ElementUI);

// to: 即将要进入的目标 用一种标准化的方式
// from: 当前导航正要离开的路由 用一种标准化的方式
// 添加全局前置导航守卫
router.beforeEach((to, from, next) => {

  // 获取JWT令牌
  const token = localStorage.getItem('jwtToken');
  console.log("JWT",token)
  if(token){//token存在
    if(to.path !== '/login' && to.path !== '/register'){
      
      next();
    }else{
      
      next('/home');
    }
    
  }else{//token不存在
    if (to.path !== '/login' && to.path !== '/register' & to.path !== '/') {
      
      next('/login')
    }else{
      
      next()
    }
  }
})

new Vue({
  el: '#app',
  router,
  store,
  render: h => h(App)
});