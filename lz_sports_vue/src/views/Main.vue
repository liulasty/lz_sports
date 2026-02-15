<template>
    <div>
        <el-container>
            <el-aside width="auto">
                <common-aside />
            </el-aside>
            <el-container>
                <el-header class="headerHeight" style="height: 50px;">
                    <common-header />
                </el-header>
                <el-main>
                    <common-tag style="padding-bottom: 2px;" />
                    <router-view />
                </el-main>
            </el-container>
        </el-container>

    </div>
</template>

<style lang="less" scoped>
.el-aside{
    width: 200px;
}

.el-container{
    padding: 0px;

    .el-header{
        padding: 0px;
        height: 50px;
        
    }
    .el-main{
        padding-top: 5px;
    }
}
</style>

<script>
import CommonAside from '../components/CommonAside.vue'
import CommonHeader from '../components/CommonHeader.vue';
import CommonTag from '@/components/CommonTag.vue';

export default {

    components: {
        CommonAside,
        CommonHeader,
        CommonTag
    },
    data() {
        return {
            
        }

    }
}
</script>