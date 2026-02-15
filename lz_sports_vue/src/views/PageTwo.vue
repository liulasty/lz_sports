<template>
    <div class="about">
        <h1>This is an about page</h1>
        <div>
            <div id="score"></div> <!-- 在这里使用评分组件 -->
        </div>
        <div class="rate">
            <div class="layui-carousel" id="test1">
                <div carousel-item>
                    <div>
                        <img class="lunboImg" alt="条目1" src="../assets/avatar.jpg">
                    </div>
                    <div><img class="lunboImg" src="../assets/bj.png" alt="条目2"></div>
                    <div><img class="lunboImg" src="../assets/huaduo.png" alt="条目3"></div>
                    <div><img class="lunboImg" src="../assets/logo.png" alt="条目4"></div>
                    <div><img class="lunboImg" src="../assets/snut.png" alt="条目5"></div>
                </div>
            </div>
        </div>
        <div>
            <div id="test2"></div> <!-- 在这里使用评分组件 -->
        </div>
        <div>
            <el-button type="primary" @click="renderLayuiTable()">申请修改</el-button>
        </div>
    </div>
</template>
<script>
import initLayui from "@/layui/layuiInit"
export default {
    data() {
        return {


        }
    },
    mounted() {
        initLayui(); // 初始化Layui
    }

}
</script>

<style>
.lunboImg {
    width: 500px;
}

.rate {
    height: 400px;
}
</style>
