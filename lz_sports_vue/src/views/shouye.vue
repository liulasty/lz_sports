<template>
    <div>
        <div id="homeBody">
            <div class="model">
                <h1>学生田径运动会报名系统</h1>
                <button class="btn" @click="ToLogin">
                    去登录
                </button>

            </div>

        </div>
    </div>
</template>
<script>
import {checkLogin} from '@/api'

export default {
    data() {
        return {
            images: [] // 存储已上传的图片
        };
    },
    mounted() {

    },
    methods: {
        ToLogin() {
            checkLogin().then((data)=> {
                console.log("检查登录",data)
                if(data.data.code == 1){
                    if(data.data.message == '令牌校验失败'){
                        
                        this.$router.push('/login')
                    }else{
                        
                        this.$router.push('/main')
                    }
                    
                }
            })

            
        },
    }
}
</script>

<style>
* {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

#homeBody {
    height: 100vh;
    background: url('../assets/huaduo.png') no-repeat center center/cover;
    background-size: cover;
    animation: animate 10s linear infinite alternate;
}

.model {
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.3);
    position: relative;
    display: flex;
    flex-direction: column;
}

.model h1 {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 8vw;
    background: url('../assets/bj.png') no-repeat center center/cover;
    -webkit-text-stroke: 4px #fff;
    color: transparent;
    background-clip: text;
    -webkit-background-clip: text;
    animation: animate 10s linear infinite alternate;
}

.model a {
    display: flex;
    /* align-items: center;
    justify-content: center; */
    text-align: center;
    width: 20%;
    margin: auto;
}

@keyframes animate {
    0% {
        background-position: 0 0;
    }

    100% {
        background-position: 0% 100%;
    }
}
</style>

