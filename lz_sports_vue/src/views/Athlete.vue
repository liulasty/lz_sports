<template>
    <div class="AthleteAndEvent">
        <div class="project_top">


            <!-- 查询组件 -->
            <div class="top_right">
                <el-input class="projectName" placeholder="请输入运动员姓名" size="small" v-model="registrationConfig.name"
                    clearable>
                </el-input>
                <el-select class="selectStatus" v-model="registrationConfig.status" size="small" placeholder="请选择参赛状态"
                    clearable>
                    <el-option v-for="item in Eligibility" :key="item.value" :label="item.label" :value="item.value">
                    </el-option>
                </el-select>
                <el-date-picker size="small" v-model="registrationConfig.date" type="datetime" placeholder="日期之后">
                </el-date-picker>
                <el-button class="selectButton" type="primary" size="small" @click="listSelectCondition">查询</el-button>
            </div>


        </div>
        <!-- 表格 -->
        <div class="projectTable">
            <el-table :data="tableData" border style="width: 100%">
                <el-table-column fixed prop="registrationTime" label="报名日期" width="320">
                </el-table-column>
                <el-table-column prop="athleteName" label="运动员名称" width="220">
                </el-table-column>
                <el-table-column prop="eventName" label="活动名称" width="160">
                </el-table-column>
                <el-table-column prop="itemName" label="项目名称" width="160">
                </el-table-column>
                <el-table-column prop="registrationStatus" label="报名状态" width="160">
                </el-table-column>
                <el-table-column label="操作" width="258">
                    <template slot-scope="scope">
                        <div v-if="applyPlayer()">
                            <el-button v-if="scope.row.registrationStatus === '审核中'" @click="examine(scope.row.id)"
                                type="primary" size="small">查看申请</el-button>
                            <el-button v-else type="warning" size="small"
                                @click="deleteRegistrationById(scope.row.id)">删除申请记录</el-button>
                        </div>
                        <div v-else>
                            <el-button v-if="scope.row.registrationStatus === '审核中'" type="warning" size="small"
                                @click="cancelRegistration(scope.row.id)">取消报名</el-button>
                            <el-button v-else-if="scope.row.registrationStatus === '通过'" type="success" @click="deleteRegistrationById(scope.row.id)" size="small">删除参加记录</el-button>
                            <el-button v-else type="info" size="small" @click="deleteRegistrationById(scope.row.id)">删除申请记录</el-button>
                        </div>

                    </template>
                </el-table-column>
            </el-table>
        </div>

        <el-dialog title="审核运动员参赛申请" :visible.sync="dialogRegistrationForm">
            <el-form :model="registrationForm" ref="registrationForm" label-width="140px" class="demo-registrationForm">
                <el-row>
                    <el-col :span="12">
                        <el-form-item label="参加项目名">
                            <el-input v-model="registrationForm.itemName" disabled></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="参加活动名">
                            <el-input v-model="registrationForm.eventName" disabled></el-input>
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row>
                    <el-col :span="12">
                        <el-form-item label="已参与人数">
                            <el-input v-model="registrationForm.num" disabled></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="最大参与人数">
                            <el-input v-model="registrationForm.maxNum" disabled></el-input>
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row>
                    <el-col :span="12">
                        <el-form-item label="申请限制">
                            <el-input v-model="registrationForm.limitation" disabled></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="申请项目所属年级">
                            <el-input v-model="registrationForm.grade" disabled></el-input>
                        </el-form-item>
                    </el-col>
                </el-row>

                <el-row>
                    <el-col :span="12">
                        <el-form-item label="申请运动员姓名">
                            <el-input v-model="registrationForm.name" disabled></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="申请运动员年龄">
                            <el-input v-model="registrationForm.age" disabled></el-input>
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row>
                    <el-col :span="12">

                        <el-form-item label="申请运动员性别" prop="gender">
                            <el-radio v-model="registrationForm.gender" label="男" disabled>男</el-radio>
                            <el-radio v-model="registrationForm.gender" label="女" disabled>女</el-radio>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="申请运动员年级">
                            <el-input v-model="registrationForm.athleteGrade" disabled></el-input>
                        </el-form-item>
                    </el-col>
                </el-row>


                <el-form-item label="申请运动员电话号码">
                    <el-input v-model="registrationForm.contact" disabled></el-input>
                </el-form-item>

                <el-form-item label="申请时间" prop="contact">
                    <el-input v-model="registrationForm.applyTime" disabled></el-input>
                </el-form-item>


                <el-form-item>
                    <el-button type="primary" @click="attendProjectById(registrationForm.id)">同意参加项目</el-button>
                    <el-button @click="refuseProjectById(registrationForm.id)">驳回申请</el-button>
                </el-form-item>
            </el-form>
        </el-dialog>
        <!-- 分页按钮 -->
        <div class="block">
            <!-- <span class="demonstration">完整功能</span> -->
            <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange"
                :current-page="registrationConfig.currentPage" :page-sizes="[3, 4, 5, 7]"
                :page-size="registrationConfig.pageSize" layout="total, sizes, prev, pager, next, jumper"
                :total=projectTotal>
            </el-pagination>
        </div>
    </div>
</template>
<script>
import { RegistrationList } from '@/api'
import { deleteRegistration } from '@/api'

import { selectApply } from '@/api'
import { attendProject } from '@/api'
import { refuseProject } from '@/api'

export default {
    data() {
        return {
            dialogUpdateForm: false,
            dialogFormVisible: false,
            dialogRegistrationForm: false,
            registrationConfig: {
                name: '',
                status: '',
                date: '',
                currentPage: 1,
                pageSize: 5,
            },
            currentPage: 1,
            projectTotal: 0,
            Eligibility: [{
                value: '审核中',
                label: '审核中'
            }, {
                value: '已通过',
                label: '已通过'
            }],
            tableData: [{
                athleteId: 4,
                athleteName: "吴晨浩",
                eventId: 74,
                eventName: "测试1",
                id: 1,
                itemId: 1,
                itemName: "项目测试",
                registrationStatus: "审核中",
                registrationTime: "无网络数据",
            }],
            registrationForm: {

            }
        }
    },
    methods: {
        //角色的分工
        applyPlayer() {
            if (this.$store.state.userInfo.type === "运动员") {
                console.log("运动员")
                return false
            }
            return true
        },
        isButtonDisabled(status) {
            if (status === '通过') return true
        },
        listSelectCondition() {

            this.selectPageDate(this.registrationConfig)
        },
        //查看申请信息
        examine(id) {
            console.log("查看申请信息的id", id);
            selectApply(id).then((data) => {

                if (data.data.code === 1) {
                    console.log("查看申请信息", data.data.data);
                    this.registrationForm=data.data.data
                    this.registrationForm.applyTime=this.DateToString(this.registrationForm.applyTime)
                    
                    this.dialogRegistrationForm = true;
                } else {
                    this.$notify.error({
                        title: '查询失败,请刷新重试!!!',
                        message: data.data.msg
                    });
                }
            })

        },
        handleSizeChange(val) {
            console.log(`每页 ${val} 条`);
            this.registrationConfig.pageSize = val;
            this.selectPageDate(this.registrationConfig)
        },
        handleCurrentChange(val) {
            console.log(`当前页: ${val}`);
            this.registrationConfig.currentPage = val;
            this.selectPageDate(this.registrationConfig)
        },
        //参赛记录查询
        selectPageDate(Config) {
            RegistrationList(Config).then((data) => {
                console.log("参赛记录查询", data.data)
                this.tableData = data.data.data.records
                this.projectTotal = data.data.data.total
                for (const key in this.tableData) {
                    if (Object.hasOwnProperty.call(this.tableData, key)) {
                        const element = this.tableData[key];
                        this.tableData[key].registrationTime = this.DateToString(element.registrationTime)
                    }
                }
            })
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
        //取消申请记录
        cancelRegistration(id){
            deleteRegistration(id).then((data) => {
                console.log("删除结果", data);
                if (data.data.code === 1) {
                    this.$notify({
                        title: '成功',
                        message: data.data.msg,
                        type: 'success'
                    });
                    this.listSelectCondition()
                } else {
                    this.$notify.error({
                        title: '失败',
                        message: data.data.msg
                    });
                }
            })
        },
        //删除记录
        deleteRegistrationById(id) {
            deleteRegistration(id).then((data) => {
                console.log("删除结果", data);
                if (data.data.code === 1) {
                    this.$notify({
                        title: '成功',
                        message: data.data.msg,
                        type: 'success'
                    });
                    this.listSelectCondition()
                } else {
                    this.$notify.error({
                        title: '删除失败',
                        message: data.data.msg
                    });
                }
            })
        },
        //同意参加项目
        attendProjectById(id) {
            attendProject(id).then((data) => {
                console.log("更新参赛响应", data)
                if (data.data.code === 1) {
                    this.$notify({
                        title: '成功',
                        message: data.data.msg,
                        type: 'success'
                    });
                    this.dialogRegistrationForm = false;
                    this.listSelectCondition()
                } else {
                    this.$notify.error({
                        title: '失败',
                        message: data.data.msg
                    });
                }
            })
        },
        //拒绝申请
        refuseProjectById(id) {
            refuseProject(id).then((data) => {
                console.log("操作结果", data);
                if (data.data.code === 1) {
                    this.$notify({
                        title: '成功',
                        message: '拒绝成功',
                        type: 'success'
                    });
                    this.dialogRegistrationForm = false;
                    this.listSelectCondition()
                } else {
                    this.$notify.error({
                        title: '拒绝失败',
                        message: data.data.msg
                    });
                }
            })
        }


    },
    mounted() {




        this.listSelectCondition(this.registrationConfig)



    },
}
</script>

<style lang="less" scoped>
.project_top {
    display: flex;
    align-items: center;
    width: 100%;

    .top_right {
        display: flex;
        justify-content: flex-end;
        align-items: center;
        margin-left: auto;

        .projectName {
            padding: 10px;
            width: 36%;

            input {
                padding-right: 5px;
            }
        }

        .selectButton {
            margin: 0px 10px;

        }

        .selectStatus {
            margin: 0px 5px;
        }
    }
}

.button-disabled {
    pointer-events: none;
    opacity: 0.5;
}
</style>
