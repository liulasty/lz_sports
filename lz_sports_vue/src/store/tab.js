export default{
    state:{
        isCollapse:false, //控制菜单的是否展开
        tabsList:[
            {
                path: '/home',
                name: 'home',
                label: '首页',
                icon: 's-home',
                url: 'Home/Home'
            },
        ] //面包屑
    },
    mutations:{
        collapseMenu(state){
            state.isCollapse = !state.isCollapse
        },
        //更新面包屑
        selectMenu(state,val){
            console.log(val,'val')
            //判断添加数据是否为首页
            if(val.name !== 'home'){
                // 该方法通常用于查找满足特定条件的数组中第一个元素的索引，具体条件是 item.name 等于 val.name。
                const index = state.tabsList.findIndex(item => item.name === val.name)
                if(index === -1){
                    state.tabsList.push(val)
                }
            }
        },

        closeTag(state,item){
            console.log(item)
            const index = state.tabsList.findIndex(val => val.name == item.name)
            state.tabsList.splice(index,1)

        }
    }
}