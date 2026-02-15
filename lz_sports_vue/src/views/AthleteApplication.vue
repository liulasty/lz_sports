<template>
    <div>
        <div v-if="athleteState">
            <el-empty v-if="athleteAdd === '申请中'" description="申请中,请等待"></el-empty>
            <el-empty v-else-if="athleteAdd === '成功'" description="申请通过,请重新登录"></el-empty>
            <el-empty v-else description="管理员拒绝了您的拒绝申请">
                <el-button type="primary" @click="reApply">重新申请</el-button>
            </el-empty>
        </div>
        <div v-else class="athlete">
            <el-form :model="ruleForm" :rules="rules" ref="ruleForm" label-width="100px" class="demo-ruleForm">
                <el-form-item label="姓名" prop="name">
                    <el-input v-model="ruleForm.name"></el-input>
                </el-form-item>

                <el-form-item label="性别" prop="gender">
                    <el-radio v-model="ruleForm.gender" label="男">男</el-radio>
                    <el-radio v-model="ruleForm.gender" label="女">女</el-radio>
                </el-form-item>

                <el-form-item label="年龄" prop="age">
                    <el-input-number v-model="ruleForm.age" controls-position="right" @change="handleChange" :min="18"
                        :max="64"></el-input-number>
                </el-form-item>

                <el-form-item label="参赛年级" prop="grade">
                    <el-radio-group v-model="ruleForm.grade">
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

                <el-form-item label="电话号码" prop="phone">
                    <el-input v-model="ruleForm.phone"></el-input>
                </el-form-item>


                <el-form-item>
                    <el-button type="primary" @click="submitForm('ruleForm')">立即申请</el-button>
                    <el-button @click="resetForm('ruleForm')">重置</el-button>
                </el-form-item>
            </el-form>
        </div>
    </div>
</template>
<script>

import { athleteAdd } from '@/api'

import { DeleteRecord } from '@/api'
import { getAthleteApply } from '@/api'
export default {
    data() {
        return {
            ruleForm: {
                name: '',
                age: '20',
                gender: '1',
                phone: '',
                userId: this.$store.state.userInfo.userId
            },
            rules: {
                name: [
                    { required: true, message: '请输入姓名', trigger: 'blur' },
                    { min: 2, max: 7, message: '长度在 2 到 7 个字符', trigger: 'blur' }
                ],
                phone: [
                    { required: true, message: '请输入电话号码', trigger: 'blur' },
                ]
            },
            athleteState: false,
            athleteAdd: "",
        };
    },
    methods: {
        submitForm(formName) {
            this.$refs[formName].validate((valid) => {
                if (valid) {
                    // this.ruleForm
                    console.log(this.ruleForm);
                    athleteAdd(this.ruleForm).then((data) => {
                        console.log("响应结果", data.data)
                        this.athleteState = true;
                        this.selectPlayerState();
                    })
                } else {
                    console.log('error submit!!');
                    return false;
                }
            });
        },
        resetForm(formName) {
            this.$refs[formName].resetFields();
        },
        handleChange(value) {
            console.log(value);
        },
        //查询申请状态
        selectPlayerState() {
            getAthleteApply(this.ruleForm.userId).then((data) => {
                console.log("申请详细信息",data.data.data);
                if (data.data.data.athleteState !== '未申请') {
                    this.athleteState = true;
                    this.athleteAdd = data.data.data.athleteState
                }
            })
        },
        reApply() {
            DeleteRecord(this.ruleForm.userId).then((data) => {
                console.log("删除信息", data)
                if (data.data.code === 1) {
                    this.athleteState = false;
                    this.$notify({
                        title: '请重新申请',
                        message: data.data.msg,
                        type: 'success'
                    });
                } else {
                    this.$notify({
                        title: '请刷新或者联系管理员',
                        message: data.data.msg,
                        type: 'error'
                    });
                }

            })
        }

    },
    mounted() {

        this.selectPlayerState()
    },
}
</script>

<style lang="less">
.athlete {
    margin-top: 20px;
}
</style>