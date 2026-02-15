<template>
    <div>
        <div class="user_top">

            <!-- 查询组件 -->
            <div class="top_right">
                <el-input class="userName" placeholder="请输入用户名关键词" size="small" v-model="userConfig.name" clearable>
                </el-input>
                <el-select class="selectStatus" v-model="userConfig.type" size="small" placeholder="请选择类型" clearable>
                    <el-option v-for="item in userEligibility" :key="item.value" :label="item.label" :value="item.value">
                    </el-option>
                </el-select>
                <el-date-picker size="small" v-model="userConfig.date" type="date" placeholder="日期之后" clearable>
                </el-date-picker>
                <el-button class="selectButton" type="primary" size="small" @click="listSelectCondition">查询</el-button>
            </div>


        </div>
        <div class="useTable">
            <el-table :data="tableData" border style="width: 100%">
                <el-table-column fixed prop="registerTime" label="注册日期" width="350">
                </el-table-column>
                <el-table-column prop="name" label="昵称" width="220">
                </el-table-column>
                <el-table-column prop="type" label="类型" width="220">
                </el-table-column>
                <el-table-column prop="state" label="状态" width="220">
                </el-table-column>
                <el-table-column label="操作" width="265">
                    <template slot-scope="scope">
                        <el-button v-if="scope.row.type === '工作人员'" type="warning" size="small" disabled>删除</el-button>
                        <el-button v-else @click="deleteUserById(scope.row)" type="warning" size="small">删除</el-button>
                        <el-button v-if="applyPlayer(scope.row)" @click="sportsEdit(scope.row)" type="info"
                            size="small">查看运动员申请</el-button>
                    </template>
                </el-table-column>
            </el-table>
        </div>

        <!-- 修改弹窗 -->
        <el-dialog title="是否允许该用户成为运动员" :visible.sync="dialogUpdateForm">
            <el-form :model="userForm" label-width="100px" class="demo-ruleForm">
                <el-form-item label="姓名" prop="name">
                    <el-input v-model="userForm.name" disabled></el-input>
                </el-form-item>

                <el-form-item label="性别" prop="gender">
                    <el-radio v-model="userForm.gender" label="男" disabled>男</el-radio>
                    <el-radio v-model="userForm.gender" label="女" disabled>女</el-radio>
                </el-form-item>

                <el-form-item label="年龄" prop="age">
                    <el-input-number v-model="userForm.age" controls-position="right" @change="handleChange" :min="18"
                        :max="64" disabled></el-input-number>
                </el-form-item>

                <el-form-item label="电话号码" prop="contact">
                    <el-input v-model="userForm.contact" disabled></el-input>
                </el-form-item>
                <el-form-item label="申请时间" prop="applyTime">
                    <el-input v-model="userForm.applyTime" disabled></el-input>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="agreeApply(userForm.userId)">允许该用户成为运动员</el-button>
                    <el-button @click="refuseApply(userForm.userId)">拒绝申请</el-button>
                </el-form-item>
            </el-form>
        </el-dialog>



        <div class="block">
            <!-- <span class="demonstration">完整功能</span> -->
            <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange" :current-page="currentPage"
                :page-sizes="[3, 4, 5, 7]" :page-size="5" layout="total, sizes, prev, pager, next, jumper" :total=userTotal>
            </el-pagination>
        </div>

    </div>
</template>
<script>
import { userList } from "@/api"
import { deleteUser } from "@/api"
import { examineSports } from "@/api"
import { getAthleteApply } from "@/api"
import { refusePlayer } from '@/api'

export default {
    mounted() {

        this.selectPageDate(this.userConfig)
    },
    methods: {
        applyPlayer(row) {
            if (row.applyState !== '申请中') return false;
            return true;
        },
        deleteUserById(row) {
            console.log(row.id)
            deleteUser(row.id).then((data) => {
                console.log("删除成功", data)
                this.selectPageDate(this.userConfig)
            })
        },
        handleChange(value) {
            console.log(value);
        },
        handleClick(row) {
            console.log(row);
        },
        listSelectCondition() {
            // console.log("搜索")
            this.selectPageDate(this.userConfig)
        },
        handleSizeChange(val) {
            // console.log(`每页 ${val} 条`);
            this.userConfig.pageSize = val;
            this.selectPageDate(this.userConfig)
        },
        handleCurrentChange(val) {
            // console.log(`当前页: ${val}`);
            this.userConfig.currentPage = val;
            this.selectPageDate(this.userConfig)
        },
        //时间格式转换
        DateToString(dateString) {
            const date = new Date(dateString);

            const year = date.getFullYear();
            const month = date.getMonth() + 1; // 月份是从0开始计数的，所以需要加1
            const day = date.getDate();
            const hours = date.getHours();
            const minutes = date.getMinutes();

            // 构建格式化后的字符串
            const formattedDate = `${year}年${month}月${day}日${hours}时${minutes}分`;

            return formattedDate
        },
        //查询分页数据
        selectPageDate(config) {
            userList(config).then((data) => {
                console.log("list", data.data.data.records)
                this.tableData = data.data.data.records
                this.userTotal = data.data.data.total
                this.tableData.forEach(item => {
                    item.registerTime = this.DateToString(item.registerTime)
                })
            })

        },
        //查看该用户的申请成为运动员
        sportsEdit(row) {
            this.dialogUpdateForm = true;
            // console.log("允许该用户成为运动员?", row);

            getAthleteApply(row.id).then((data) => {
                console.log("审核响应", data);
                this.userForm.name = data.data.data.name
                this.userForm.gender = data.data.data.gender
                this.userForm.age = data.data.data.age
                this.userForm.contact = data.data.data.contact
                this.userForm.applyTime = this.DateToString(data.data.data.applyTime)
                this.userForm.athleteId = data.data.data.athleteId
                this.userForm.userId = data.data.data.userId
                console.log(this.userForm)
            }).catch(() => {
                this.$message({
                    type: 'warning',
                    message: '查询失败!'
                });
            })
        },
        //同意
        agreeApply(id) {
            examineSports(id).then((data) => {
                console.log("审核响应", data);
                this.$message({
                    type: 'success',
                    message: '修改成功!'
                });
                this.listSelectCondition();
                this.dialogUpdateForm = false;
            }).catch(() => {
                this.$message({
                    type: 'warning',
                    message: '修改失败!'
                });
            })

        },
        //拒绝
        refuseApply(id) {
            refusePlayer(id).then((data) => {
                console.log("审核响应", data);
                this.$message({
                    type: 'success',
                    message: '拒绝成功!'
                });
                this.listSelectCondition();
            }).catch(() => {
                this.$message({
                    type: 'warning',
                    message: '拒绝失败!'
                });
            })
            this.dialogUpdateForm = false;

        }
    },

    data() {
        return {
            dialogUpdateForm: false,
            currentPage: 1,
            userTotal: 0,
            tableData: [{
                id: '',
                date: '2016-05-02',
                name: '王小虎',
                type: '工作人员',
                state: '未激活'
            }],
            dialogFormVisible: false,
            userConfig: {
                name: '',
                type: '',
                date: '',
                currentPage: 1,
                pageSize: 5,
            },
            userEligibility: [{
                value: '管理员',
                label: '管理员'
            }, {
                value: '用户',
                label: '用户'
            }, {
                value: '运动员',
                label: '运动员'
            }],
            useForm: {

            },
            userForm: {
                athleteId: 4,
                userId: 2,
                name: "吴晨浩",
                age: "20",
                gender: "男",
                contact: "男",
                state: "成功",
                applyTime: "2023-12-19T09:25:20.000+00:00",
            },
        }
    },


}
</script>

<style lang="less" scoped>
.user_top {
    display: flex;
    align-items: center;
    width: 100%;
    margin-bottom: 20px;

    .top_right {
        display: flex;
        justify-content: flex-end;
        align-items: center;
        margin-left: auto;


        .selectButton {
            margin: 0px 10px;

        }

        .selectStatus {
            margin: 0px 5px;
        }
    }
}
</style>