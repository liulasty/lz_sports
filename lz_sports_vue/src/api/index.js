import http from '../utils/request'

// import aliyun from '../utils/ailiyun.js'

export const getData = () => {
    return http.get('/event/mainPageData')
}

export const uploadAvatar = (file) => {
    const formData = new FormData();
    formData.append('file', file);

    return http.post('/img/uploadAvatar', formData);
};

export const updateImg = (formData) => {
    return http.post('/img/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },// 设置请求头为multipart/form-data类型
    })
}

export const updateEvent = (eventId, event) => {
    return http.put("/event/" + eventId, event)
}

export const deleteImg = (deleteImg) => {
    return http.delete('/img/delete', { data: deleteImg })
}

export const deleteEvent = (deleteEventId) => {
    console.log("删除的事件编号", deleteEventId);
    return http.delete('/event/' + deleteEventId)
}

export const getEventImg = (eventId) => {
    return http.get('/img/getEventImg/' + eventId)
}

export const addEvent = (eventList) => {
    return http.post('/event/EventList', eventList)
}

export const selectEventById = (id) => {
    return http.get('/event/' + id)
}


export const list = (listSelectCondition) => {
    console.log("查询参数", listSelectCondition);
    return http.get('/event/page', {
        params: listSelectCondition,
    })
}

export const login = (userInfo) => {
    console.log("登录参数", userInfo)
    return http.post('/user/login', userInfo)
}

export const logout = () => {
    console.log("退出");
    return http.delete('user/logout')
}

export const checkLogin = () => {
    console.log("检查jwt")
    return http.get('user/checkLogin')
}

export const register = (userInfo) => {
    console.log("注册参数", userInfo)
    return http.post('/user/register', userInfo)
}

export const deleteUser = (id) => {
    console.log("删除", id)
    return http.delete('/user/' + id)
}

export const userList = (listSelectCondition) => {
    console.log("查询参数", listSelectCondition);
    return http.get('/user/page', {
        params: listSelectCondition,
    })
}

export const athleteAdd = (athlete) => {
    console.log("athleteAdd参数", athlete);
    return http.post('/athlete', athlete)
}

export const getAthleteApply = (id) => {
    console.log("获取运动员申请记录", id);
    return http.get('/athlete/apply/' + id)
}

export const getAthlete = (id) => {
    console.log("获取运动员详细信息", id);
    return http.get('/athlete/' + id)
}

export const updateAthlete = (data) => {
    console.log("修改运动员信息,运动员信息", data)
    return http.put('/athlete/' + data.athleteId, data)
}

export const examineSports = (id) => {
    console.log("运动员审核通过", id);
    return http.get('/user/athlete/' + id)
}

export const refusePlayer = (id) => {
    console.log("运动员审核不通过", id);
    return http.delete('/user/athlete/' + id)
}



export const DeleteRecord = (id) => {
    console.log("删除运动员申请记录", id)
    return http.delete("/athlete/" + id)
}


export const projectList = (listSelectCondition) => {
    console.log("查询参数", listSelectCondition);
    return http.get('/project/page', {
        params: listSelectCondition,
    })
}

export const eventTypes = () => {
    console.log("活动类型")
    return http.get("/event/eventType")
}

export const addProject = (projectList) => {
    console.log("添加项目")
    return http.post('/project', projectList)
}

export const selectProject = (id) => {
    console.log("根据编号查询项目")
    return http.get('/project/' + id)
}

export const editProject = (data) => {
    console.log("编辑项目")
    return http.put('/project/' + data.id, data)
}


export const deleteProject = (id) => {
    console.log("删除项目")
    return http.delete("/project/" + id)
}

export const joinProject = (data) => {
    console.log("运动员参加项目")
    return http.post("/project/join", data)
}

export const refuseProject = (id) => {
    console.log("拒绝该运动员参加该项目")
    return http.put("/registration/refuse/" + id)
}

export const selectApply = (id) => {
    console.log("查询运动员申请参赛记录", id)
    return http.get("/registration/" + id)
}

export const RegistrationList = (data) => {
    console.log("参赛记录搜索", data)

    if (data.date != "") {
        let dateString = data.date;
        let date = new Date(dateString);
        date.setHours(date.getHours() + 8);

        const data1 = {
            name: data.name,
            status: data.status,
            date: date,
            currentPage: 1,
            pageSize: 5,
        }

        return http.get("/registration/page", {
            params: data1,
        })
    }


    return http.get("/registration/page", {
        params: data,
    })
}

export const getAthleteRegistrationTotal = (id) => {
    console.log("查询运动员申请参赛记录总数,运动员编号", id)
    return http.get("/registration/athlete/" + id)
}

export const deleteRegistration = (id) => {
    console.log("删除参赛记录")
    return http.delete("/registration/" + id)
}

export const attendProject = (id) => {
    console.log("同意参加项目", id)
    return http.put("/registration/" + id)
}




// export const loadImageAsBase64 =(url) =>{
//     return aliyun.get(url)
// }