<template>
    <div>
        <h1>pageOne.vue</h1>
        <div class=""> <!-- 注意：这一层元素并不是必须的 -->
            <span id="testView"></span>
            <div id="laydate"></div>
        </div>
        <div class="calendar">
            <el-button type="primary" @click="previousMonth">上个月</el-button>
            <el-button type="primary" @click="nextMonth">下个月</el-button>
            <el-button type="primary" @click="festivalDates">生成文件</el-button>
            <table>
                <thead>
                    <tr>
                        <th v-for="day in daysOfWeek" :key="day">{{ day }}</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="(week, index) in calendar" :key="index">
                        <td v-for="date in week" :key="date.key"
                            :class="{ today: date.isToday, differentMonth: date.differentMonth }">
                            {{ date.day > 0 ? date.day : '' }}
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>

</template>
<script>
import initLayui from "@/layui/layuiInit"

export default {
    data() {
        return {
            calendar: [],
            weeks: [],
            date: new Date()
        }
    },
    computed: {
        daysOfWeek() {
            return ['日', '一', '二', '三', '四', '五', '六'];
        },

    },
    mounted() {
        this.calendar = this.getCalendar();
        initLayui(); // 初始化Layui
        layui.use('laydate', function () {
            var laydate = layui.laydate;

            //执行一个laydate实例
            laydate.render({
                elem: '#laydate' //指定元素
                , type: 'date',
                position: 'static'
                , change: function (value, date) { //监听日期被切换
                    lay('#testView').html(value);
                }
                , theme: 'molv'
                , calendar: true
            })
        });

    },
    methods: {
        previousMonth() {
            this.date.setMonth(this.date.getMonth() - 1);
            this.calendar = this.getCalendar();
            console.log("calendar", this.calendar)
        },
        nextMonth() {
            this.date.setMonth(this.date.getMonth() + 1);
            this.calendar = this.getCalendar();
            console.log("calendar", this.calendar)
        },
        isToday(date) {
            const today = new Date();
            return date.toDateString() === today.toDateString();
        },
        getCalendar() {
            const year = this.date.getFullYear();
            const month = this.date.getMonth();
            const firstDayOfMonth = new Date(year, month, 1);
            const lastDayOfMonth = new Date(year, month + 1, 0);
            const startDate = new Date(firstDayOfMonth);
            startDate.setDate(startDate.getDate() - startDate.getDay());

            const calendar = [];
            while (startDate <= lastDayOfMonth) {
                const week = [];
                for (let i = 0; i < 7; i++) {
                    const day = new Date(startDate);
                    week.push({
                        day: day.getDate(),
                        isToday: this.isToday(day),
                        differentMonth: day.getMonth() !== month,
                        key: day.toISOString()
                    });
                    startDate.setDate(startDate.getDate() + 1);
                }
                calendar.push(week);

            }
            return calendar;
        },
        festivalDates() {
            

        }

    },
}
</script>
<style>
.weekend {
    background-color: lightyellow;
}

#testView {
    display: none;
}

table {
    border-collapse: collapse;
    width: 100%;
}

th,
td {
    padding: 8px;
    text-align: center;
}

.today {
    background-color: lightblue;
}

.differentMonth {
    color: lightgray;
}

/* 调整字体大小 */
table {
    font-size: 14px;
}

th,
td {
    font-size: 12px;
}
</style>