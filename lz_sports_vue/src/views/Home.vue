<template>
    <el-row>
        <el-col :span="8" style="padding-right: 5px;">
            <el-card class="box-card" :body-style="{ height: '220px' }">
                <div class="userImg">
                    <img :src="avatarSrc" alt="">
                    <div class="user-info">
                        <p class="username">{{ userInfo.userName }}</p>
                        <p class="access">{{ userInfo.type }}</p>
                    </div>
                </div>
                <div class="login-info">
                    <p>时间：<span>2023年11月10日2时10分</span></p>
                    <p>地点：<span>教务处</span></p>
                </div>
            </el-card>
            <el-card style="margin-top: 18PX;height: 310PX;">
                <el-table :data="tableData" style="width: 100%" height="250">
                    <el-table-column fixed prop="date" label="活动报名开始时间" width="190">
                    </el-table-column>
                    <el-table-column v-for="(val, key) in tableLabel" :key="key" :prop="key" :label="val" width="100" />
                </el-table>
            </el-card>
        </el-col>
        <el-col :span="16">
            <div class="num">
                <el-card v-for="item  in countData" :key="item.name"
                    :body-style="{ display: 'flex', padding: '0px', 'align-items': 'center', height: '60px', padding: '5px', width: '190px', margin: '5px 5px' }">
                    <i class="icon" :class="`el-icon-${item.icon}`" :style="{ background: item.color }" />
                    <div class="detail">
                        <p class="price">¥{{ item.value }}</p>
                        <p class="desc">{{ item.name }}</p>
                    </div>
                </el-card>
            </div>

            <el-card :body-style="{ padding: '0px', width: '850px', }">
                <div ref="echarts1" class="echarts1" style="height: 220px">

                </div>
            </el-card>
            <div class="graph">
                <el-card :body-style="{ padding: '5px', width: '480px', height: '170px' }">
                    <div ref="echarts2" style="height: 150px;width: 470px;">

                    </div>
                </el-card>
                <el-card :body-style="{ padding: '5px', width: '360px', height: '170px' }">
                    <div ref="echarts3" style="height: 160px;width: 350px;">

                    </div>
                </el-card>
            </div>



        </el-col>
    </el-row>
</template>
<script>

import * as echarts from 'echarts';
import { mapState } from 'vuex';
import { getData } from '@/api';


export default {
    components: {},
    data() {
        return {
            tableData: [{
                date: '2016-05-03',
                name: '活动名称',
                type: '参赛资格',
                fee: '0'
            },],
            countData: [
                {
                    name: "运动员总数",
                    value: 1234,
                    icon: "success",
                    color: "#2ec7c9",
                },
                {
                    name: "活动总数",
                    value: 210,
                    icon: "star-on",
                    color: "#ffb980",
                },
                {
                    name: "项目总数",
                    value: 1234,
                    icon: "s-goods",
                    color: "#5ab1ef",
                },
                {
                    name: "本月新增运动员",
                    value: 1234,
                    icon: "success",
                    color: "#2ec7c9",
                },
                {
                    name: "本月新增活动",
                    value: 210,
                    icon: "star-on",
                    color: "#ffb980",
                },
                {
                    name: "本月新增项目",
                    value: 1234,
                    icon: "s-goods",
                    color: "#5ab1ef",
                },
            ],
            tableLabel: {
                name: '活动名称',
                type: '参赛资格',
                fee: '费用'
            },
            userInfo: {
                userName: '',
                type: '',
            }
        }
    },
    mounted() {

        const sportsUser = localStorage.getItem('sportsUser')
        const parsedUser = JSON.parse(sportsUser);
        console.log("parsedUser", parsedUser)
        this.userInfo = parsedUser
        console.log("userInfo", this.userInfo)

        getData().then((data) => {
            const dataAll = data.data.data
            this.tableData = dataAll.tableData
            // console.log("tableData", this.tableData)
            this.countData[0].value = dataAll.totalNumberAthletes;
            this.countData[1].value = dataAll.totalNumberActivities;
            this.countData[2].value = dataAll.totalNumberProjects;
            this.countData[3].value = dataAll.newAthletesAddedMonth;
            this.countData[4].value = dataAll.newActivitiesAddedMonth;
            this.countData[5].value = dataAll.newProjectsAddedMonth;


            this.tableData.forEach(item => {
                item.date = this.DateToString(item.date)
            })
            const echarts1 = echarts.init(this.$refs.echarts1);
            // 指定图表的配置项和数据
            var option = {
                title: {
                    text: '各项报名方式的活动统计'
                },
                // tooltip: 用于配置图表的工具提示（Tooltip）。
                tooltip: {
                    // trigger: 'axis': 这表示工具提示将在鼠标悬停在数据点上时触发，并显示与轴相对应的数据值。这通常用于线型图表，以帮助用户查看每个系列在不同数据点上的具体值。
                    trigger: 'axis'
                },
                // legend: 配置图例，用于标识每个系列的名称。
                legend: {
                    // data: 包含了图例的数据，这些数据与之前配置的各个系列的名称相对应。在这里，图例包括 'Email'、'Union Ads'、'Video Ads'、'Direct' 和 'Search Engine'。
                    data: ['Email', 'Union Ads', 'Video Ads', 'Direct', 'Search Engine']
                },
                // grid: 配置图表的网格，用于控制图表的位置和大小。
                grid: {
                    // left, right, bottom: 这些属性指定了图表的边距，即图表与容器边界之间的空白区域。这些值通常以百分比或像素表示。
                    left: '3%',
                    right: '4%',
                    bottom: '3%',
                    // containLabel: true: 如果设置为 true，则表示网格将包含轴标签和标题等内容。这确保了网格的大小会自动调整，以便完全容纳所有内容。
                    containLabel: true
                },
                // toolbox: 配置图表的工具箱，用于添加一些交互式功能。
                toolbox: {
                    // feature: 用于配置工具箱中的功能。在这里，只定义了一个功能，即 saveAsImage，它允许用户保存图表为图像文件。工具箱可以包含其他功能，如数据视图、数据区域缩放、刷新等，根据您的需求添加。
                    feature: {
                        saveAsImage: {}
                    }
                },
                xAxis: {
                    type: 'category',
                    boundaryGap: false,
                    data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
                },
                yAxis: {
                    type: 'value'
                },
                series: [
                    // name: 每个系列的名称，将显示在图例中，用于标识每个系列的数据。
                    // type: 'line': 指定了这些系列是线型的，即它们将在图表上以线的形式表示。
                    // stack: 'Total': 这表示这些系列将在同一堆栈中堆叠。在图表中，这意味着它们的值将相加在一起，以显示它们在每个数据点上的总和。
                    // data: 每个系列的具体数据点。在示例中，每个系列包括七个数据点，分别对应不同的时间或类别。
                    // {
                    //     name: 'Email',
                    //     type: 'line',
                    //     stack: 'Total',
                    //     data: [120, 132, 101, 134, 90, 230, 210]
                    // }
                ]
            };

            const { orderData, userData, doughnutData } = dataAll
            // console.log("orderData", dataAll.orderData)
            const xAxis = Object.keys(orderData.data[0])

            option.xAxis = {
                data: orderData.date,
            }
            option.legend = {
                data: xAxis,
            }
            xAxis.forEach(key => {
                option.series.push({
                    name: key,
                    data: orderData.data.map(item => item[key]),
                    type: 'line',
                })
            })

            // 使用刚指定的配置项和数据显示图表。
            echarts1.setOption(option);

            //柱状图
            const echarts2 = echarts.init(this.$refs.echarts2);

            option = {
                tooltip: {
                    trigger: 'axis',
                    axisPointer: {
                        type: 'shadow',
                        label: {
                            show: true
                        }
                    }
                },
                toolbox: {
                    show: true,
                    feature: {
                        mark: { show: true },
                        dataView: { show: true, readOnly: false },
                        magicType: { show: true, type: ['line', 'bar'] },
                        // restore: { show: true },
                        saveAsImage: { show: true }
                    }
                },
                calculable: true,
                legend: {
                    data: ['新增学生', '新增运动员'],
                    itemGap: 2,
                    left: 'center',
                    top: 0,
                    align: 'auto',
                    width: '165px',
                    type: 'scroll',
                },
                grid: {
                    top: '27%',
                    left: '1%',
                    right: '1%',
                    bottom: '5%',
                    containLabel: true
                },
                xAxis: [
                    {
                        type: 'category',
                        data: userData.map(item => item.date)
                    }
                ],
                yAxis: [
                    {
                        type: 'value',
                        name: '人数 ',
                        nameTextStyle: {
                            align: 'left',
                        },
                    }
                ],
                series: [
                    {
                        name: '新增学生',
                        type: 'bar',
                        data: userData.map(item => item.addUser)
                    },
                    {
                        name: '新增运动员',
                        type: 'bar',
                        data: userData.map(item => item.addAthlete)
                    }
                ]
            };

            echarts2.setOption(option);
            let data_pie = []; // 创建一个空的数组来存储数据
            let total_pie = 0;
            // 使用循环将用户输入的数据添加到数组中
            for (let i = 0; i < doughnutData.length; i++) {
                data_pie.push({ value: doughnutData[i].nums, name: doughnutData[i].type });
                total_pie = total_pie + doughnutData[i].nums;
            }

            let data_end = [
                {
                    // make an record to fill the bottom 50%
                    value: total_pie,
                    itemStyle: {
                        // stop the chart from rendering this piece
                        color: 'none',
                        decal: {
                            symbol: 'none'
                        }
                    },
                    label: {
                        show: false
                    }
                }
            ];
            data_pie.push(data_end[0]);
            //饼图
            const echarts3 = echarts.init(this.$refs.echarts3);

            // 配置项
            option = {
                tooltip: {
                    // trigger: '参加项目运动员占比'
                },
                legend: {
                    top: '2%',
                    left: 'center',
                    // doesn't perfectly work with our tricks, disable it
                    // 启用选中模式
                    selectedMode: false,
                    type: 'scroll',
                },
                series: [
                    {
                        name: 'Access From',
                        type: 'pie',
                        // 半径
                        radius: ['40%', '80%'],
                        // 中心
                        center: ['50%', '80%'],
                        // 调整起始角度
                        startAngle: 180,

                        // formatter(param) { return param.name + ' (' + param.percent * 2 + '%)'; } 是用来格式化标签内容的函数。
                        // 在这里，param 是传入的参数对象，包含了标签所需的数据。通过这个函数，可以自定义标签的显示内容。
                        // 在这个例子中，标签的内容包括了数据项的名称和一个经过修改的百分比。这里的 param.percent * 2 对百分比进行了修改，将其乘以 2。
                        label: {
                            //表示显示标签，如果设置为 false 则会隐藏标签。

                            show: false,

                            formatter(param) {
                                // correct the percentage
                                return param.name + ' (' + param.percent * 2 + '%)';
                            }
                        },
                        data: data_pie,
                        tooltip: {
                            trigger: 'item', // 鼠标悬停到扇形上触发显示
                            formatter(param) {
                                // 自定义提示框内容的显示
                                return '名称：' + param.name + '<br/>' +
                                    '数值：' + param.value + '<br/>' +
                                    '占比：' + (param.percent * 2).toFixed(2) + '%';
                            }
                        }
                    },

                ],


            };
            echarts3.setOption(option);
        })

    },
    methods: {
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
        }

    },
    // 在组件B中计算属性或mapState来访问状态  

    computed: {
        ...mapState({
            avatarSrc: state => state.userInfo.avatarSrc
        })

    }

}
</script>

<style lang="less" scoped>
* {
    margin: 0;
    padding: 0;
}

.userImg {
    padding-bottom: 8px;
    margin-bottom: 8px;
    border-bottom: 1px solid #ccc;
    display: flex;
    align-items: center;

    img {
        margin-right: 40px;
        width: 100px;
        height: 100px;
        border-radius: 50%;
    }

    .user-info {
        .username {
            font-size: 18px;
            margin-bottom: 4px;
        }

        .access {
            color: #888;
        }

    }

}

.login-info {
    p {
        line-height: 38px;
        font-size: 14px;
        color: #999999;

        span {
            color: #666666;
            margin-left: 60px;
        }
    }
}

.num {
    display: flex;
    flex-wrap: wrap;

    .icon {
        width: 60px;
        height: 60px;
        font-size: 55px;
        text-align: center;
        padding: auto;
        color: #000;
        border-radius: 16%;
        justify-content: center;
    }

    .detail {
        height: 60px;
        display: flex;
        flex-direction: column;
        justify-content: center;
        padding: 0px;
        margin-top: 0px;
        margin-left: 20px;

        .price {
            padding: 0px;
            margin-top: 0px;
            font-size: 24px;
            margin-bottom: 0px;
        }

        .desc {
            font-size: 12px;
            color: #999;
            text-align: center;
        }
    }

    .el-card {
        width: 31%;
        margin-right: 10px;
        margin-bottom: 10px;
        justify-content: space-between;
    }

}

.graph {
    display: flex;
    //左右贴边
    justify-content: space-between;
    padding: 0px;


}
</style>
