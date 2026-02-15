<template>
    <div class="tabs">
            <el-tag 
            v-for="(item , index) in tags " 
            :key="item.label" 
            :closable="item.name !== 'home'" 
            :effect="$route.name === item.name ? 'dark': 'plain'" 
            @click="changeMenu(item)"
            @close="handleClose(item,index)"
            size="small" >
                {{ item.label }}
            </el-tag>
    </div>
</template>

<script>
import { mapState,mapMutations } from 'vuex';
export default{
    name: 'CommonTag',
    data(){
        return{
        }
    },
    computed:{
        ...mapState({
                tags: state => state.tab.tabsList
            })
    },
    mounted() {
        console.log('面包屑',this.$route.name,this.tags)
    },
    methods:{
        // 调用了一个名为mapMutations的函数，并传入了一个参数['closeTag']
        ...mapMutations(['closeTag']),
        //点击tag跳转效果
        changeMenu(item){
            this.$router.push({name : item.name})
        },
        // 点击tag删除效果
        handleClose(item,index){
            //调用store中store函数
            this.closeTag(item)
            const length = this.tags.length
            //删除之后跳转
            if(item.name !== this.$route.name){
                return 
            }
            //删除最后一项
            if(index === length){
                this.$router.push({
                    name:this.tags[index-1].name
                })
            }else{
                this.$router.push({
                    name: this.tags[index].name
                })
            }
        }
    }
}
</script>
<style lang="less" scoped>
    .tabs{
        padding: 5px;
        .el-tag{
            margin-right: 15px;
            // 用于指定鼠标在元素上悬停时的光标样式。
            cursor: pointer;
        }
    }
</style> 