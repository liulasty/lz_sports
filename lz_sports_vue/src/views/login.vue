<template>
    <div id="LogionBody">
        <div class="demo-image__placeholder">
            <div class="block">
                <div class="image-container" @click="goShowye">
                    <el-image :src="src" class="custom-image"></el-image>
                    <div class="text-overlay">
                        中小学生田径运动会报名系统
                    </div>
                </div>
            </div>
        </div>
        <div class="container panel-active">
            <!-- 注册 -->
            <div class="formBox register">
                <form action="#" class="form" id="form1">
                    <h2 class="title">注 册</h2>
                    <input type="text" placeholder="用户名" class="input" />
                    <input type="email" placeholder="邮箱" class="input" />
                    <input type="password" placeholder="密码" class="input" />
                    <button class="btn" @click.prevent="registerCheck">注册</button>
                </form>
            </div>

            <!-- 登录 -->
            <div class="formBox login">
                <form action="#" class="form" id="form2">
                    <h2 class="title">登 陆</h2>
                    <input type="text" placeholder="邮箱" class="input" />
                    <input type="password" placeholder="密码" class="input" />
                    <!-- <a href="#" class="link">忘记密码？</a> -->
                    <button class="btn" @click.prevent="loginCheck">登陆</button>
                </form>
            </div>

            <!-- 浮层 -->
            <div class="overlay-box">
                <div class="overlay">
                    <div class="panel over-left">
                        <button class="btn" id="signIn">已有账号？立即登陆</button>
                    </div>
                    <div class="panel over-right">
                        <button class="btn" id="signUp">无账号？前往注册</button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>




<script>
import { login } from '@/api';
import { register } from '@/api'


export default {
    data() {
        return {
            images: [], // 存储已上传的图片
            loginInfo: {
                username: '',
                password: '',
                email: ''
            },
            src: 'shouyesuolv.jpg'
        };
    },
    mounted() {
        this.beforeLogin();
    },
    methods: {
        //添加监听器
        beforeLogin() {
            const signInBtn = document.getElementById("signIn");
            const signUpBtn = document.getElementById("signUp");
            const container = document.querySelector(".container");

            signInBtn.addEventListener("click", () => {
                container.classList.remove("panel-active");
            });

            signUpBtn.addEventListener("click", () => {
                container.classList.add("panel-active");
            });
        },
        //登录
        loginCheck() {
            console.log("登录")

            const form = document.getElementById("form2");
            const username = form.elements[0].value;
            const password = form.elements[1].value;
            if (this.validateStr(username, "u") !== "username" & this.validateStr(username, "e") !== "e-mail") {
                this.$message({
                    showClose: true,
                    message: '用户名格式错误请重新输入',
                    type: 'error'
                });

                return;
            } else {
                if (this.validateStr(password, "p") == "password") {
                    this.loginInfo.username = username
                    this.loginInfo.password = password

                    login(this.loginInfo).then((data) => {
                        console.log("登录后的参数", data.data);
                        if (data.data.code === 1) {
                            // 存储JWT令牌
                            localStorage.setItem('jwtToken', data.data.data.token);
                            localStorage.setItem('sportsUser', JSON.stringify(data.data.data));
                            
                            this.$store.commit('loginUser',data.data.data);
                            
                            this.$router.push('/main')

                            this.$message({
                                showClose: true,
                                message: '恭喜你，登录成功成功',
                                type: 'success'
                            })
                        } else {
                            this.$message({
                                showClose: true,
                                message: data.data.msg,
                                type: 'error'
                            });
                        }
                    });
                } else {
                    this.$message({
                        showClose: true,
                        message: '密码错误请重新输入',
                        type: 'error'
                    });


                }
            }
        },

        registerCheck() {
            const form = document.getElementById("form1");
            const username = form.elements[0].value;
            const password = form.elements[2].value;
            const email = form.elements[1].value;

            if (this.validateStr(username, "u") !== "username" &
                this.validateStr(email, "e") !== "e-mail" &
                this.validateStr(password, "p") !== "password") {
                this.$message({
                    showClose: true,
                    message: '用户名格式错误请重新输入',
                    type: 'error'
                });

                return;
            } else {
                this.loginInfo.username = username;
                this.loginInfo.password = password;
                this.loginInfo.email = email;
                register(this.loginInfo).then((data) => {
                    console.log(data.data)
                    if (data.data.code != 0) {
                        this.$message({
                            showClose: true,
                            message: data.data.msg,
                            type: 'success'
                        })
                    } else {
                        this.$message({
                            showClose: true,
                            message: data.data.msg,
                            type: 'error'
                        })
                    }
                })
            }
        },

        //校验账号密码
        validateStr(input, type) {
            var regexEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            var regexUsername = /^\w{3,8}$/;
            var regexPassword = /^(?=.*[a-zA-Z])(?=.*\d)[a-zA-Z\d]{8,16}$/;
            if (regexEmail.test(input) & type == 'e') {
                return "e-mail";
            } else if (regexUsername.test(input) & type == 'u') {
                return "username";
            } else if (regexPassword.test(input) & type == 'p') {
                return "password";
            } else {
                return false;
            }
        },

        goShowye(){
            this.$router.push('/')
        }
    }
}

</script>

<style>
* {
    margin: 0;
    padding: 0;
}

.demo-image__placeholder {
    padding-top: 2%;
    padding-left: 2%;
}

.custom-image {
    width: 180px;

}

.image-container {
    position: relative;
}

.text-overlay {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    font-size: 16px;
    color: white;
}

#LogionBody {
    display: flex;
    width: 100vw;
    height: 100vh;
    overflow: hidden;
    background-image: url('../assets/huaduo.png');
    background-size: cover;
}

.container {
    width: 100%;
    max-width: 758px;
    height: 420px;
    position: relative;
    margin: auto;
}


.formBox {
    height: 100%;
    position: absolute;
    top: 0;
    transition: all 0.6s ease-in-out;
}

.login {
    left: 0;
    width: 50%;
    z-index: 2;
}

.register {
    left: 0;
    opacity: 0;
    width: 50%;
    z-index: 1;
}

.form {
    background-color: #e9e9e9;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-direction: column;
    padding: 0 3rem;
    height: 100%;
    text-align: center;
}

.input {
    background-color: #fff;
    border: none;
    padding: 0.9rem 0.9rem;
    margin: 0.5rem 0;
    width: 100%;
}

.title {
    font-weight: 300;
    margin: 0;
    margin-bottom: 1.25rem;
}

.link {
    color: #333;
    font-size: 16px;
    margin: 1.5rem 0;
    text-decoration: none;
}


.btn {
    background-color: #095c91;
    background-image: linear-gradient(90deg, #095c91 0%, #0393a3 74%);
    border-radius: 5px;
    border: none;
    color: #e9e9e9;
    cursor: pointer;
    font-size: 0.8rem;
    font-weight: bold;
    letter-spacing: 0.1rem;
    padding: 0.9rem 4rem;
    text-transform: uppercase;
    transition: transform 80ms ease-in;
}

.form>.btn {
    margin-top: 1.5rem;
}

.btn:active {
    transform: scale(0.95);
}



.overlay-box {
    height: 100%;
    left: 50%;
    overflow: hidden;
    position: absolute;
    top: 0;
    transition: transform 0.6s ease-in-out;
    width: 50%;
    z-index: 100;
}

.overlay {
    background-color: rgba(255, 255, 255, 0.25);
    height: 100%;
    left: -100%;
    position: relative;
    transform: translateX(0);
    transition: transform 0.6s ease-in-out;
    width: 200%;
}

.panel {
    width: 50%;
    height: 100%;
    align-items: center;
    display: flex;
    flex-direction: column;
    justify-content: center;
    position: absolute;
    text-align: center;
    top: 0;
    transform: translateX(0);
    transition: transform 0.6s ease-in-out;
}

.over-left {
    transform: translateX(-20%);
}

.over-right {
    right: 0;
    transform: translateX(0);
}



.panel-active .login {
    transform: translateX(100%);
}

.panel-active .register {
    opacity: 1;
    transform: translateX(100%);
    z-index: 3;
}

.panel-active .overlay-box {
    transform: translateX(-100%);
}

.panel-active .overlay {
    transform: translateX(50%);
}

.panel-active .over-left {
    transform: translateX(0);
}

.panel-active .over-right {
    transform: translateX(20%);
}
</style>
