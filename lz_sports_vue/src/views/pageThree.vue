<template>
    <div class="three">
        <div class="transfer-box">
            <div class="layui-row layui-col-space15">
                <div class="layui-col-md6">
                    <div class="layui-panel">
                        <div style="padding: 30px;">
                            <div class="left-panel">
                                <h3>左侧列表</h3>
                                <ul>
                                    <li v-for="item in leftItems" :key="item.id" @click="moveToRight(item)">
                                        {{ item.name }}
                                    </li>
                                </ul>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="layui-col-md6">
                    <div class="layui-panel">
                        <div style="padding: 30px;">
                            <div class="right-panel">
                                <h3>右侧列表</h3>
                                <ul>
                                    <li v-for="item in rightItems" :key="item.id" @click="moveToLeft(item)">
                                        {{ item.name }}
                                    </li>
                                </ul>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

        </div>
        <div>
            <el-button type="primary" @click="renderLayuiTable()">申请修改1</el-button>
            <el-button type="primary" @click="renderLayuiTable2()">申请修改2</el-button>
            <el-button type="primary" @click="renderLayuiTable3()">申请修改3</el-button>
        </div>

        <div>
            <table id="demo" lay-filter="test"></table>
        </div>
    </div>

</template>

<script>
// import initLayui from "@/layui/layuiInit"
export default {
    data() {
        return {
            leftItems: [
                { id: 1, name: 'Apple' },
                { id: 2, name: 'Banana' },
                { id: 3, name: 'Orange' },
                { id: 4, name: 'Grape' },
            ],
            rightItems: [],
            tableData: [
                { id: 1, name: 'Alice', age: 25 },
                { id: 2, name: 'Bob', age: 30 },
                // 其他数据...
            ]
        };
    },
    mounted() {
        initLayui()
    },
    methods: {
        moveToRight(item) {
            const index = this.leftItems.indexOf(item);
            if (index > -1) {
                this.leftItems.splice(index, 1);
                this.rightItems.push(item);
            }
        },
        moveToLeft(item) {
            const index = this.rightItems.indexOf(item);
            if (index > -1) {
                this.rightItems.splice(index, 1);
                this.leftItems.push(item);
            }
        },
        async getData() {
            // 向后台请求数据
            // 假设 fetchDataFromBackend 是一个向后台请求数据的方法
            this.tableData = await this.fetchDataFromBackend();

            // 数据更新后重新渲染 Layui 表格组件
            this.$nextTick(() => {
                this.renderLayuiTable();
            });
        },
        renderLayuiTable() {
            layui.use('table', () => {
                var table = layui.table;
                table.render({
                    elem: '#demo',
                    data: this.tableData,
                    cols: [[
                        { field: 'id', title: 'ID' },
                        { field: 'name', title: 'Name' },
                        { field: 'age', title: 'Age' }
                        // 其他列...
                    ]]
                });
            });
        },
        renderLayuiTable2() {
            layui.use('table', () => {
                var table = layui.table;
                table.render({
                    elem: '#demo',
                    data: this.tableData,
                    cols: [[
                        { field: 'id', title: 'ID' },
                        { field: 'name', title: '名字' },
                        { field: 'age', title: '年龄' },
                        { field: 'age', title: '年龄' }
                        // 其他列...
                    ]]
                });
            });
        },
        renderLayuiTable3() {
            this.tableData= [
                { id: 1, name: '阿里', age: 25 },
                { id: 2, name: '鲍勃', age: 30 },
                // 其他数据...
            ]
        }

    },
};
</script>

<style scoped>
.transfer-box {
    /* Flex 布局是一种强大的 CSS 布局模型，通过使用 display: flex */
    display: flex;
}

.left-panel,
.right-panel {
    flex: 1;
    padding: 20px;
}

ul {
    list-style-type: none;
    padding: 0;
}

li {
    cursor: pointer;
    margin-bottom: 10px;
}
</style>