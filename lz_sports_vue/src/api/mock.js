import homeApi from './mockServeData/home'
import mock from "mockjs";

//定义请求拦截
// mock.mock('http://image-upload-and-management.oss-cn-beijing.aliyuncs.com/f373fffe-073d-40b4-98bc-ccf3a0f2cba8-image0.png','get',function(){
//     //拦截到请求后的路径
//     console.log('请求以拦截')
//     return '1'
// })

mock.mock('/api/home/getData','get',function(){
    //拦截到请求后的路径
    console.log('集合请求以拦截')
    
    
    return homeApi.getStatisticalData()
})