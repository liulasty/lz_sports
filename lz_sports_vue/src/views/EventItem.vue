<!-- eslint-disable no-unused-vars -->
<template>
    <div class="projectItem">
        <div class="project_top">
            <div v-if="applyPlayer()" class="top_left">
                <el-button type="primary" size="small" icon="el-icon-circle-plus-outline"
                    @click="dialogFormVisible = true">添加项目</el-button>
            </div>

            <!-- 查询组件 -->
            <div class="top_right">
                <el-input class="projectName" placeholder="请输入项目名称关键词" size="small" v-model="projectConfig.name" clearable>
                </el-input>
                <el-select class="selectStatus" v-model="projectConfig.event" size="small" placeholder="请选择活动类型" clearable>
                    <el-option v-for="item in Eligibility" :key="item.value" :label="item.label" :value="item.label">
                    </el-option>
                </el-select>
                <el-date-picker size="small" v-model="projectConfig.date" type="date" placeholder="日期之后" clearable>
                </el-date-picker>
                <el-button class="selectButton" type="primary" size="small" @click="searchRecords">查询</el-button>
            </div>


        </div>
        <!-- 项目列表表格 -->
        <div class="projectTable">
            <el-table :data="tableData" border style="width: 100%">
                <el-table-column fixed align="center" prop="range" label="报名的时间范围" width="370">
                </el-table-column>
                <el-table-column prop="name" label="项目名称" width="100">
                </el-table-column>
                <el-table-column prop="rate" label="报名比率" width="80">
                </el-table-column>
                <el-table-column prop="limitation" label="限制条件" width="80">
                </el-table-column>
                <el-table-column prop="grade" label="参加年级" width="80">
                </el-table-column>
                <el-table-column prop="projectStart" label="比赛开始时间" width="185">
                </el-table-column>
                <!-- <el-table-column prop="maxAttendance" label="最大参加人数" width="120">
                </el-table-column>
                <el-table-column prop="attendance" label="已申请参加人数" width="120">
                </el-table-column> -->
                <el-table-column prop="eventName" label="运动会名称" width="100">
                </el-table-column>
                <el-table-column prop="state" label="报名方式" width="80">
                </el-table-column>
                <!-- <el-table-column prop="start" label="报名开始日期" width="170">
                </el-table-column>
                <el-table-column prop="end" label="报名截止日期" width="170">
                </el-table-column> -->

                <!-- <el-table-column prop="projectEnd" label="比赛结束时间" width="185">
                </el-table-column> -->
                <el-table-column label="操作" width="210" fixed="right">
                    <template slot-scope="scope">
                        <div v-if="applyPlayer()">
                            <el-button @click="editProjectById(scope.row)" type="primary" size="medium">编辑</el-button>
                            <el-button @click="deleteProjectById(scope.row)" type="warning" size="medium">删除</el-button>
                        </div>
                        <div v-else-if="isEligibility(scope.row)">
                            <el-button v-if="scope.row.isJoin === '未参加'" @click="attendProjectById(scope.row)"
                                type="primary" size="medium">
                                {{ scope.row.isJoin }}
                            </el-button>
                            <el-button v-else type="warning" size="medium" disabled>{{ scope.row.isJoin }} </el-button>

                        </div>
                        <div v-else>
                            <el-button type="info" size="medium" disabled>{{ cannotReason }}</el-button>
                        </div>
                    </template>

                </el-table-column>
            </el-table>
        </div>
        <!-- 分页按钮 -->
        <div class="block">
            <!-- <span class="demonstration">完整功能</span> -->
            <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange"
                :current-page="projectConfig.currentPage" :page-sizes="[3, 4, 5, 7]" :page-size="projectConfig.pageSize"
                layout="total, sizes, prev, pager, next, jumper" :total=projectTotal>
            </el-pagination>
        </div>

        <!-- 修改弹窗 -->
        <el-dialog title="修改项目" :visible.sync="dialogUpdateForm" :before-close="handleClose">
            <el-form :model="projectForm" :rules="rules" ref="projectForm" label-width="100px" class="demo-ruleForm">
                <el-form-item label="项目名称" prop="name">
                    <el-input v-model="projectForm.name"></el-input>
                </el-form-item>
                <el-form-item label="活动" prop="event">
                    <el-select v-model="projectForm.event" placeholder="请选择活动">
                        <el-option v-for="item in Eligibility" :key="item.value" :label="item.label"
                            :value="item.value"></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="参赛限制" prop="limitation">
                    <el-radio-group v-model="projectForm.limitation">
                        <el-radio label="男" name="limitationUpdate"></el-radio>
                        <el-radio label="女" name="limitationUpdate"></el-radio>
                        <el-radio label="不限" name="limitationUpdate"></el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="参赛年级" prop="grade">
                    <el-radio-group v-model="projectForm.grade">
                        <el-radio label="一年级" name="gradeUpdate"></el-radio>
                        <el-radio label="二年级" name="gradeUpdate"></el-radio>
                        <el-radio label="三年级" name="gradeUpdate"></el-radio>
                        <el-radio label="四年级" name="gradeUpdate"></el-radio>
                        <el-radio label="五年级" name="gradeUpdate"></el-radio>
                        <el-radio label="六年级" name="gradeUpdate"></el-radio>
                        <el-radio label="七年级" name="gradeUpdate"></el-radio>
                        <el-radio label="八年级" name="gradeUpdate"></el-radio>
                        <el-radio label="九年级" name="gradeUpdate"></el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="最大参赛人数" prop="maxAttendance">
                    <el-input-number v-model="projectForm.maxAttendance" :min="5" :max="50"></el-input-number>
                </el-form-item>
                <el-form-item label="项目照片" prop="uploadImg">
                    <ImageUploader ref="imageSet2" />
                </el-form-item>

                <el-form-item>
                    <el-button type="primary" @click="updateForm()">确定修改</el-button>
                    <el-button @click="resetForm('projectForm')">重置</el-button>
                </el-form-item>
            </el-form>
        </el-dialog>

        <!-- 添加弹窗 -->
        <el-dialog title="添加项目" :visible.sync="dialogFormVisible" :before-close="handleClose">
            <el-form :model="projectForm" :rules="rules" ref="projectForm" label-width="100px" class="demo-ruleForm">
                <el-form-item label="项目名称" prop="name">
                    <el-input v-model="projectForm.name"></el-input>
                </el-form-item>
                <el-form-item label="活动" prop="event">
                    <el-select v-model="projectForm.event" placeholder="请选择活动">
                        <el-option v-for="item in Eligibility" :key="item.value" :label="item.label"
                            :value="item.value"></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="参赛限制" prop="limitation">
                    <el-radio-group v-model="projectForm.limitation">
                        <el-radio label="男" name="limitation"></el-radio>
                        <el-radio label="女" name="limitation"></el-radio>
                        <el-radio label="不限" name="limitation"></el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="比赛时间" required>
                    <el-date-picker v-model="projectForm.date" type="datetimerange" range-separator="至"
                        start-placeholder="开始日期" end-placeholder="结束日期">
                    </el-date-picker>
                </el-form-item>
                <el-form-item label="最大参赛人数" prop="maxAttendance">
                    <el-input-number v-model="projectForm.maxAttendance" :min="5" :max="50"></el-input-number>
                </el-form-item>
                <el-form-item label="参赛年级" prop="grade">
                    <el-radio-group v-model="projectForm.grade">
                        <el-radio label="一年级" name="grade"></el-radio>
                        <el-radio label="二年级" name="grade"></el-radio>
                        <el-radio label="三年级" name="grade"></el-radio>
                        <el-radio label="四年级" name="grade"></el-radio>
                        <el-radio label="五年级" name="grade"></el-radio>
                        <el-radio label="六年级" name="grade"></el-radio>
                        <el-radio label="七年级" name="grade"></el-radio>
                        <el-radio label="八年级" name="grade"></el-radio>
                        <el-radio label="九年级" name="grade"></el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="项目照片" prop="uploadImg">
                    <ImageUploader ref="imageSet1" />
                </el-form-item>

                <el-form-item>
                    <el-button type="primary" @click="submitForm('projectForm')">立即创建</el-button>
                    <el-button @click="resetForm('projectForm')">重置</el-button>
                </el-form-item>
            </el-form>
        </el-dialog>
    </div>
</template>
<script>
import ImageUploader from '@/components/ImageUploader.vue';
import { projectList } from '@/api'
import { eventTypes } from '@/api'
import { addProject } from '@/api'
import { selectProject } from '@/api'
import { editProject } from '@/api'
import { deleteProject } from '@/api'
import { joinProject } from '@/api'
import { getAthlete } from '@/api'


export default {
    components: { ImageUploader },
    data() {
        return {
            dialogUpdateForm: false,
            dialogFormVisible: false,
            projectConfig: {
                name: "",
                event: "",
                date: "",
                currentPage: 1,
                pageSize: 5,
            },
            currentPage: 1,
            projectTotal: 0,
            Eligibility: [],
            tableData: [{
                id: '',
                start: '',
                date: '2016-05-02',
                name: '王小虎',
                eventName: '工作人员',
                eventId: '',
                state: '未激活',
                range: '',
                rate: '0/20',
            }],
            projectForm: {
                name: '',
                event: '',
                limitation: '',
                grade: '',
                maxAttendance: 10
            },
            rules: {
                name: [
                    { required: true, message: '请输入项目名称', trigger: 'blur' },
                    { min: 2, max: 6, message: '长度在 2 到 6 个字符', trigger: 'blur' }
                ],
                event: [
                    { required: true, message: '请选择活动区域', trigger: 'change' }
                ]
            },
            playerInfo: {

            },
            cannotReason: '未知',
        }
    },
    methods: {
        //搜索记录
        searchRecords() {
            this.projectConfig.currentPage = 1
            this.listSelectCondition()
        },
        //查询分页
        listSelectCondition() {
            // 初始化 tableData
            this.tableData = [];
            console.log("查询条件", this.projectConfig)
            projectList(this.projectConfig).then((data) => {
                // console.log("查询到的数据", data.data)
                const records = data.data.data.records;
                this.projectTotal = data.data.data.total;
                records.forEach(element => {
                    element.start = this.DateToString(element.start)
                    element.end = this.DateToString(element.end)
                    element.range = this.formatDateTime(element.start, element.end)
                    element.projectStart = this.modifyDateTime(element.projectStart)
                    element.projectEnd = this.DateToString(element.projectEnd)
                    element.rate = this.createFraction(element.attendance, element.maxAttendance)
                    this.tableData.push(element);
                });
            })
        },
        //创建参赛率
        createFraction(num1, num2) {
            var numerator = Math.max(0, Math.min(num1, num2)); // 确保分子最小为0且最大不超过分母
            return numerator + "/" + num2;
        },
        //报名的时间范围
        formatDateTime(startDate, endDate) {

            return startDate + "——" + endDate;
        },
        //比赛日期
        modifyDateTime(dateTimeString) {
            var date = new Date(dateTimeString);
            var year = date.getFullYear();
            var month = date.getMonth() + 1; // 月份从0开始，所以需要加1
            var day = date.getDate();
            var hour = date.getHours();

            // 修改为下午
            if (hour >= 12) {
                return year + "年" + month + "月" + day + "日下午";
            } else {
                return year + "年" + month + "月" + day + "日上午";
            }
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
        //字符串到时间
        parseDateFromString(dateString) {
            var parts = dateString.match(/(\d+)/g);
            // parts数组会包含年、月、日、小时和分钟的数字
            return new Date(parts[0], parts[1] - 1, parts[2], parts[3], parts[4]);
        },
        //角色的分工
        applyPlayer() {
            if (this.$store.state.userInfo.type === "运动员") {
                // console.log("运动员")
                return false
            }
            return true
        },
        //判断运动员是否有资格
        isEligibility(date) {
            // console.log("这行信息", date)
            // console.log("运动员信息", this.playerInfo)
            const NOW = new Date();
            const start = this.parseDateFromString(date.start)
            const end = this.parseDateFromString(date.end)
            if (start > NOW || end < NOW) {
                // console.log("时间未在范围内",start,end)
                this.cannotReason = '未在报名时间范围内'
                return false
            }

            if (this.playerInfo.grade !== date.grade) {
                // console.log("年级不符合",this.playerInfo.grade,date.grade)
                this.cannotReason = '未在报名年级范围内'
                return false
            }

            if (date.limitation != '不限') {
                if (date.limitation !== this.playerInfo.gender) {
                    this.cannotReason = '未在报名性别范围内'
                    return false
                }
            }

            if (date.attendance >= date.maxAttendance) {
                if (date.attendance >= date.maxAttendance) {
                    // console.log("年级不符合",this.playerInfo.grade,date.grade)
                    this.cannotReason = '参与人数已达最多'
                    return false
                }
            }
            this.cannotReason = '符合'
            return true
        },
        //获取运动员信息
        getPlayerInfo() {
            if (this.$store.state.userInfo.type !== '工作人员') {
                getAthlete(this.$store.state.userInfo.userId).then((data) => {
                    // console.log("运动员信息",data.data.data);
                    this.playerInfo = data.data.data;
                })
            }

        },
        //分页按钮
        handleSizeChange(val) {
            console.log(`每页 ${val} 条`);
            this.projectConfig.pageSize = val;
            this.listSelectCondition(this.projectConfig);
        },
        //换页
        handleCurrentChange(val) {
            console.log(`当前页: ${val}`);
            this.projectConfig.currentPage = val;
            console.log("currentPage", this.tableData)

            this.listSelectCondition(this.projectConfig);
        },
        //表单关闭
        handleClose(done) {
            this.$confirm('确认关闭？', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(date => {
                    console.log(date)
                    done();

                })
                .catch(error => {
                    console.log(error)
                });
        },
        //提交添加表单
        submitForm(formName) {
            this.$refs[formName].validate((valid) => {
                if (valid) {
                    this.$refs.imageSet1.uploadImages()
                } else {
                    console.log('error submit!!');
                    return false;
                }

                this.projectForm.imageUrls = this.$refs.imageSet1.imageUrls;
                this.projectForm.deleteImagesUrls = this.$refs.imageSet1.deleteImagesUrls;
                console.log('完整数据', this.projectForm)

                addProject(this.projectForm).then((data) => {
                    if (data.data.code != 0) {
                        this.$notify({
                            title: '成功',
                            message: '添加成功',
                            type: 'success'
                        });
                        console.log("添加完成", data)
                    } else {
                        this.$notify.error({
                            title: '添加失败',
                            message: data.data.msg
                        });
                    }
                    this.dialogFormVisible = false;
                    this.listSelectCondition()
                    this.resetForm(formName)
                })

            });
        },
        //清空表单
        resetForm(formName) {
            this.$refs[formName].resetFields();
        },
        //运动员参加项目
        attendProjectById(row) {
            // console.log("参加", row)

            this.$confirm('活动项目:' + row.name + "——" + row.grade + "——" + row.eventName
                + "<br> 报名截止时间——>" + row.end
                + "<br>若成功报名,超过报名截止时间或者通过审核后则无法取消报名，请确认是否继续申请", '参加项目', {
                confirmButtonText: '确认参加',
                cancelButtonText: '取消',
                dangerouslyUseHTMLString: true,
                type: 'info'
            }).then(() => {
                const joinData = { eventId: row.eventId, projectId: row.projectId, userId: this.$store.state.userInfo.userId }
                joinProject(joinData).then((data) => {
                    console.log("参加项目的响应结果", data);
                    if (data.data.code === 1) {
                        this.$notify({
                            title: '参加项目',
                            message: data.data.msg,
                            type: 'success'
                        });
                        this.listSelectCondition()
                    } else {
                        this.$notify.error({
                            title: '参加项目',
                            message: data.data.msg
                        });
                        this.listSelectCondition()
                    }

                })
            }).catch(() => {
                this.$message({
                    type: 'info',
                    message: '已取消参加'
                });
            });


        },
        //删除
        deleteProjectById(row) {
            console.log("删除", row)

            deleteProject(row.projectId).then((data) => {
                if (data.data.code != 0) {
                    this.$notify({
                        title: '删除成功',
                        message: data.data.msg,
                        type: 'success'
                    });
                    console.log("删除成功", data)

                    this.dialogUpdateForm = false;
                    if (this.projectTotal % this.projectConfig.pageSize == 1 && this.projectConfig.currentPage > 1) {

                        this.handleCurrentChange(this.projectConfig.currentPage - 1)
                    } else {
                        this.listSelectCondition()
                    }

                } else {
                    this.$notify.error({
                        title: '删除失败，请刷新重试',
                        message: data.data.msg
                    });
                }

            })
        },

        //编辑项目
        editProjectById(row) {
            console.log("编辑", row)
            this.dialogUpdateForm = true;

            selectProject(row.projectId).then((data) => {
                console.log(data.data.data)

                const project = data.data.data;
                this.projectForm.id = row.projectId;
                this.projectForm.name = project.name;
                this.projectForm.event = project.event;
                this.projectForm.limitation = project.limitation;
                this.projectForm.grade = project.grade;
                this.$refs.imageSet2.initImgSet()
                for (let index = 0; index < project.addImage.length; index++) {
                    const element = project.addImage[index];
                    this.$refs.imageSet2.addImage(element)
                }
            })
            //
        },
        //提交修改
        updateForm() {
            this.$refs.imageSet2.uploadImages()
            this.projectForm.imageUrls = this.$refs.imageSet2.imageUrls;
            this.projectForm.deleteImagesUrls = this.$refs.imageSet2.deleteImagesUrls;
            console.log("修改后的·数据", this.projectForm);
            editProject(this.projectForm).then((data) => {
                console.log("响应", data)
                if (data.data.code != 0) {
                    this.$notify({
                        title: '修改成功',
                        message: data.data.msg,
                        type: 'success'
                    });
                    console.log("修改完成", data)

                    this.dialogUpdateForm = false;
                    this.listSelectCondition()
                } else {
                    this.$notify.error({
                        title: '修改失败',
                        message: data.data.msg
                    });
                }
            })
        }
    },
    mounted() {
        eventTypes().then((data) => {
            this.Eligibility = [];
            const types = data.data.data
            for (let index = 0; index < types.length; index++) {
                const element = types[index];

                this.Eligibility.push({ 'value': element.EventID, 'label': element.EventName })
                // console.log("label", this.Eligibility)
            }
        })
        this.listSelectCondition()
        this.getPlayerInfo()
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
            padding: 5px;
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
