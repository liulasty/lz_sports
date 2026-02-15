<template>
    <div>
        <div class="form" style="width: 1280px; align-items: center;">
            <el-form :model="infoForm" ref="infoForm" :label-position="labelPosition" label-width="150px"
                class="demo-infoForm" style="width: 600px;">
                <el-form-item label="姓名" prop="name">
                    <el-input v-model="infoForm.name" class="form-item-input" disabled></el-input>
                </el-form-item>

                <el-form-item label="性别" prop="gender">
                    <div class="form-item-input">
                        <el-radio v-model="infoForm.gender" label="男" disabled>男</el-radio>
                        <el-radio v-model="infoForm.gender" label="女" disabled>女</el-radio>
                    </div>
                </el-form-item>

                <el-form-item label="年龄" prop="age">
                    <el-input v-model="infoForm.age" controls-position="left" disabled></el-input>
                </el-form-item>

                <el-form-item label="参赛年级" prop="grade">
                    <el-radio-group v-model="infoForm.grade" disabled>
                        <el-radio label="一年级" name="grade" style="height: 30px;"></el-radio>
                        <el-radio label="二年级" name="grade" style="height: 30px;"></el-radio>
                        <el-radio label="三年级" name="grade" style="height: 30px;"></el-radio>
                        <el-radio label="四年级" name="grade" style="height: 30px;"></el-radio>
                        <el-radio label="五年级" name="grade" style="height: 30px;"></el-radio>
                        <el-radio label="六年级" name="grade" style="height: 30px;"></el-radio>
                        <el-radio label="七年级" name="grade" style="height: 30px;"></el-radio>
                        <el-radio label="八年级" name="grade" style="height: 30px;"></el-radio>
                        <el-radio label="九年级" name="grade" style="height: 30px;"></el-radio>
                    </el-radio-group>
                </el-form-item>

                <el-form-item label="电话号码" prop="contact">
                    <el-input v-model="infoForm.contact" disabled></el-input>
                </el-form-item>


                <el-form-item>
                    <el-button type="primary" @click="submitForm('infoForm')">申请修改</el-button>
                </el-form-item>
            </el-form>
        </div>
        <div>
            <el-dialog title="修改活动" :visible.sync="dialogUpdateForm">
                <el-form :model="infoUpdateForm" ref="infoUpdateForm" label-width="100px" class="demo-infoUpdateForm">
                    <el-form-item label="姓名" prop="name">
                        <el-input v-model="infoUpdateForm.name"></el-input>
                    </el-form-item>

                    <el-form-item label="性别" prop="gender">
                        <el-radio v-model="infoUpdateForm.gender" label="男">男</el-radio>
                        <el-radio v-model="infoUpdateForm.gender" label="女">女</el-radio>
                    </el-form-item>

                    <el-form-item label="年龄" prop="age">
                        <el-input-number v-model="infoUpdateForm.age" controls-position="right" :min="18"
                            :max="64"></el-input-number>
                    </el-form-item>

                    <el-form-item label="参赛年级" prop="grade">
                        <el-radio-group v-model="infoUpdateForm.grade">
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

                    <el-form-item label="电话号码" prop="contact">
                        <el-input v-model="infoUpdateForm.contact"></el-input>
                    </el-form-item>


                    <el-form-item>
                        <el-button type="primary" @click="UpdateInfo()">申请修改</el-button>
                        <el-button @click="dialogUpdateForm = false">取消修改</el-button>
                    </el-form-item>
                </el-form>

            </el-dialog>
        </div>

    </div>
</template>
<script>
import { getAthlete } from '@/api'
import { getAthleteRegistrationTotal } from '@/api'

import { updateAthlete } from '@/api'
export default {
    data() {
        return {
            dialogUpdateForm: false,
            labelPosition: 'right',
            infoUpdateForm: {
                athleteId: 1,
            },
            infoForm: {
                name: '',
                age: '20',
                gender: '1',
                contact: '',
                userId: this.$store.state.userInfo.userId
            },
            playerInfo: {
            },

        }
    },
    mounted() {


        this.getInfo()
    },
    methods: {
        getInfo() {
            getAthlete(this.$store.state.userInfo.userId).then((data) => {
                console.log("运动员信息", data.data.data);
                this.infoForm = data.data.data;
            })
        },
        submitForm() {

            getAthleteRegistrationTotal(this.infoForm.athleteId).then((data) => {

                if (data.data.data == 0) {
                    console.log("运动员参加项目总数", data.data.data > 0);
                    this.dialogUpdateForm = true
                    this.infoUpdateForm = this.infoForm
                } else {
                    this.$notify.error({
                        title: '失败',
                        message: '有比赛记录在无法修改'
                    });
                }
            })

        },

        UpdateInfo() {
            console.log("提交修改运动员信息", this.infoUpdateForm);
            updateAthlete(this.infoUpdateForm).then((data) => {
                if (data.data.code == 1) {
                    console.log("修改运动员信息成功", data.data.data > 0);
                    this.dialogUpdateForm = true
                    this.infoUpdateForm = this.infoForm
                    this.$message({
                        showClose: true,
                        message: '修改成功，请重新登录',
                        type: 'success'
                    })
                    this.$router.push('/login')
                } else {
                    this.$notify.error({
                        title: '修改运动员信息失败',
                        message: data.data.msg
                    });
                }
            })
        }
    }
}
</script>
