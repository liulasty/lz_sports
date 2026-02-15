<template>
    <div class="manage">

        <div class="event_top">
            <div class="top_left">
                <el-button type="primary" size="small" icon="el-icon-circle-plus-outline"
                    @click="dialogFormVisible = true">添加活动</el-button>
            </div>

            <!-- 查询组件 -->
            <div class="top_right">
                <el-input class="eventName" placeholder="请输入活动关键词" size="small" v-model="eventList.name" clearable>
                </el-input>
                <el-select class="selectStatus" v-model="eventList.type" size="small" placeholder="请选择活动类型" clearable>
                    <el-option v-for="item in Eligibility" :key="item.value" :label="item.label" :value="item.value">
                    </el-option>
                </el-select>
                <el-date-picker size="small" v-model="eventList.date" type="date" placeholder="日期之后" clearable>
                </el-date-picker>
                <el-button class="selectButton" type="primary" size="small" @click="listSelectCondition">查询</el-button>
            </div>


        </div>
        <!-- 添加Form -->
        <el-dialog title="添加活动" :visible.sync="dialogFormVisible" :before-close="handleClose">
            <el-form :model="ruleForm" v-loading="loading" :rules="rules" ref="ruleForm" label-width="100px"
                class="demo-ruleForm">
                <el-form-item label="活动名称" prop="name">
                    <el-input v-model="ruleForm.name"></el-input>
                </el-form-item>
                <el-form-item label="活动时间" required>
                    <el-date-picker v-model="ruleForm.date1" type="datetimerange" range-separator="至"
                        start-placeholder="开始日期" end-placeholder="结束日期">
                    </el-date-picker>
                </el-form-item>
                

                <el-form-item label="参赛资格" prop="type">
                    <el-radio-group v-model="ruleForm.type">
                        <el-radio label="线上报名" name="type"></el-radio>
                        <el-radio label="单位报名" name="type"></el-radio>
                        <el-radio label="线下报名" name="type"></el-radio>
                        <el-radio label="其他" name="type"></el-radio>
                    </el-radio-group>
                </el-form-item>

                <el-form-item label="活动费用" prop="fee">
                    <el-input-number v-model="ruleForm.fee" :min="0" :max="9"></el-input-number>
                </el-form-item>
                <el-form-item label="活动照片" prop="img">
                    <ImageUploader ref="imageSet1" />
                </el-form-item>

                <el-form-item>
                    <el-button type="primary" @click="submitForm('ruleForm')">立即创建</el-button>
                    <el-button type="info" @click="resetForm('ruleForm')">重置</el-button>
                </el-form-item>
            </el-form>    
        </el-dialog>

        <el-dialog title="修改活动" :visible.sync="dialogUpdateForm" :before-close="handleClose">
            <el-form :model="updateForm" :rules="rules" ref="updateForm" label-width="100px" class="demo-ruleForm">
                <el-form-item label="活动名称" prop="name">
                    <el-input v-model="updateForm.name"></el-input>
                </el-form-item>
                <el-form-item label="活动时间" required>
                    <el-date-picker v-model="updateForm.date1" type="datetimerange" range-separator="至"
                        start-placeholder="开始日期" end-placeholder="结束日期">
                    </el-date-picker>
                </el-form-item>

                <el-form-item label="参赛资格" prop="type">
                    <el-radio-group v-model="updateForm.type">
                        <el-radio label="线上报名" name="type"></el-radio>
                        <el-radio label="单位报名" name="type"></el-radio>
                        <el-radio label="线下报名" name="type"></el-radio>
                        <el-radio label="其他" name="type"></el-radio>
                    </el-radio-group>
                </el-form-item>

                <el-form-item label="活动费用" prop="fee">
                    <el-input-number v-model="updateForm.fee" :min="0" :max="9"></el-input-number>
                </el-form-item>
                <el-form-item label="活动照片" prop="img">
                    <ImageUploader :childData="parentMessage" ref="imageSet2" />
                </el-form-item>

                <el-form-item>
                    <el-button type="primary" @click="updateEventForm()">确定修改</el-button>
                    <el-button type="info" @click="resetForm('updateForm')">重置</el-button>
                </el-form-item>
            </el-form>
        </el-dialog>

        <!-- 表格 -->
        <el-table :data="tableData" border style="width: 100%">
            <el-table-column prop="eventName" label="活动名称" width="150">
            </el-table-column>
            <el-table-column prop="registrationStart" label="活动开始时间" width="250">
            </el-table-column>
            <el-table-column prop="eligibility" label="参赛资格" width="150">
            </el-table-column>
            <el-table-column prop="registrationFee" label="参赛费用" width="220">
            </el-table-column>
            <el-table-column prop="registrationDeadline" label="活动截止报名时间" width="250">
            </el-table-column>
            <el-table-column fixed="right" label="操作" width="200">
                <template slot-scope="scope">
                    <el-button type="primary" @click="editEvent(scope.row)" size="small">编辑</el-button>
                    <el-button @click="deleteEvent(scope.row)" type="warning" size="small">删除</el-button>
                </template>
            </el-table-column>
        </el-table>
        <div class="block">
            <!-- <span class="demonstration">完整功能</span> -->
            <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange" :current-page="currentPage"
                :page-sizes="[3, 4, 5, 7]" :page-size="5" layout="total, sizes, prev, pager, next, jumper"
                :total=eventTotal>
            </el-pagination>
        </div>
    </div>
</template>
<script>

import ImageUploader from '@/components/ImageUploader.vue';
import { addEvent } from '@/api';
import { list } from '@/api';
import { selectEventById } from '@/api';
import { deleteEvent } from '@/api'
import { updateEvent } from '@/api'



export default {
    components: { ImageUploader },
    data() {
        return {
            loading: false,
            currentPage: 1,
            eventTotal: 0,
            newImageUrl: 'http://image-upload-and-management.oss-cn-beijing.aliyuncs.com/wallhaven-3zvypv.jpg',

            parentMessage: {
                //图片的集合
                imageUrls: [],
                //需要删除的图片
                deleteImagesUrls: [],
                //原有的图片
                OriginalPhotos: [],
            },

            tableData: [
            ],
            eventList: {
                name: "",
                type: "",
                date: "",
                currentPage: 1,
                pageSize: 5,
            },

            Eligibility: [{
                value: '线上报名',
                label: '线上报名'
            }, {
                value: '单位报名',
                label: '单位报名'
            }, {
                value: '线下报名',
                label: '线下报名'
            }, {
                value: '其他',
                label: '其他'
            }],

            dialogTableVisible: false,
            dialogFormVisible: false,
            dialogUpdateForm: false,

            ruleForm: {
                name: '',
                date1: [new Date(), new Date()],
                type: [],
                fee: 0,
                limitation:'',
            },
            rules: {
                name: [
                    { required: true, message: '请输入活动名称', trigger: 'blur' },
                    { min: 3, max: 9, message: '长度在 3 到 7 个字符', trigger: 'blur' }
                ],
                date1: [
                    { required: true, message: '请选择时间', trigger: 'change' }
                ],
                type: [
                    { required: true, message: '请选择一个活动性质', trigger: 'change' }
                ],
                fee: [
                    { required: true, message: '请输入活动费用', trigger: 'blur' },
                ]
            },

            updateForm: {
                id: 1,
                name: '',
                date1: [new Date(), new Date()],
                type: [],
                fee: 0,
                imageUrls: []
            }


        };
    },
    mounted() {

        this.selectPageDate(this.eventList)
    },
    methods: {
        handleClose(done) {
            this.$confirm('确认关闭？')
                .then(_ => {
                    done();

                })
                .catch(_ => { });
        },
        handleSizeChange(val) {
            console.log(`每页 ${val} 条`);
            this.eventList.pageSize = val;
            this.selectPageDate(this.eventList);
        },
        handleCurrentChange(val) {
            console.log(`当前页: ${val}`);
            this.eventList.currentPage = val;
            console.log("currentPage", this.eventList)

            this.selectPageDate(this.eventList);
        },
        //子组件
        accessChildData() {
            // 访问子组件的数据

            // 调用子组件的方法
            this.$refs.imageSet1.testImg();

        },

        //提交表单,添加活动
        submitForm(formName) {
            this.loading = true;
            this.$refs[formName].validate((valid) => {

                if (valid) {
                    this.$refs.imageSet1.uploadImages()
                    this.ruleForm.imageUrls = this.$refs.imageSet1.imageUrls;
                    this.ruleForm.deleteImagesUrls = this.$refs.imageSet1.deleteImagesUrls;
                    this.$refs.imageSet1.initImgSet()

                } else {
                    console.log('error submit!!');
                    return false;
                }

            });


            console.log('完整数据', this.ruleForm)
            addEvent(this.ruleForm).then((data) => {
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

                this.listSelectCondition()
            })
            this.loading = false;

            this.dialogFormVisible = false
            this.resetForm(formName)

        },
        resetForm(formName) {

            this.$refs[formName].resetFields();



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
        //搜索
        listSelectCondition() {
            this.eventList.currentPage = 1;

            this.selectPageDate(this.eventList)
        },
        //查询分页数据
        selectPageDate(config) {
            list(config).then((data) => {
                console.log("list", data.data.data.records)
                this.tableData = data.data.data.records
                this.eventTotal = data.data.data.total
                this.tableData.forEach(item => {
                    item.registrationStart = this.DateToString(item.registrationStart)
                    item.registrationDeadline = this.DateToString(item.registrationDeadline)
                })
            })

        },
        //事件编辑
        editEvent(data) {
            this.dialogUpdateForm = true;
            this.updateForm.id = data.eventId;
            selectEventById(data.eventId).then((data) => {
                console.log("编辑·", data)
                if (data.data.code != 1) {
                    this.dialogUpdateForm = false;
                    this.$notify.error({
                        title: '失败',
                        message: '请刷新重试'
                    });

                    return;
                }

                const eventDate = data.data.data
                console.log("需要编辑的数据·", eventDate)

                this.updateForm.name = eventDate.name;
                this.updateForm.fee = eventDate.fee;

                this.updateForm.date1 = [
                    this.dataAddEight(new Date(eventDate.date))
                    ,
                    this.dataAddEight(new Date(eventDate.end))
                ]
                this.updateForm.type = eventDate.type;
                this.$refs.imageSet2.initImgSet()
                for (let index = 0; index < eventDate.imageUrls.length; index++) {
                    const element = eventDate.imageUrls[index];
                    this.$refs.imageSet2.addImage(element)
                }
                console.log("需要修改的图片", this.$refs.imageSet2.imageUrls)
                console.log("填充", this.updateForm)

            })

        },
        //处理时间
        dataAddEight(date) {
            date.setHours(date.getHours() - 8);
            return date
        },
        //修改活动
        updateEventForm() {
            this.$refs.imageSet2.uploadImages()
            console.log("全部图片", this.$refs.imageSet2.imageUrls)
            console.log("需要删除的图片", this.$refs.imageSet2.deleteImagesUrls)
            this.updateForm.imageUrls = this.$refs.imageSet2.imageUrls;
            this.updateForm.deleteImagesUrls = this.$refs.imageSet2.deleteImagesUrls;
            console.log("修改的表单数据", this.updateForm);
            updateEvent(this.updateForm.id, this.updateForm).then((data) => {
                console.log(data)
                if (data.data.code != 0) {
                    this.$notify({
                        title: '成功',
                        message: '修改成功',
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

        },
        //删除活动
        deleteEvent(event) {

            this.$confirm('此操作将永久删除, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'success'
            }).then(() => {
                deleteEvent(event.eventId).then((data) => {
                    console.log("删除结果", data);
                    if (data.data.code === 1) {
                        this.$notify({
                            title: '成功',
                            message: '删除成功',
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
            }).catch(() => {
                this.$message({
                    type: 'info',
                    message: '已取消删除'
                });
            });

        },


    },

};
</script>
<style lang="less" scoped>
.event_top {
    display: flex;
    align-items: center;
    width: 100%;

    .top_right {
        display: flex;
        justify-content: flex-end;
        align-items: center;
        margin-left: auto;

        .eventName {
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
</style> 
