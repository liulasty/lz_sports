<template>
  <div class="header-container">
    <div class="l-content">
      <el-button @click="handleMenu" style="margin-right: 10px;" icon="el-icon-menu" size="medium"></el-button>
      <!-- 面包雪 -->
      <el-breadcrumb separator="/">
        <el-breadcrumb-item v-for="item in tags" :key="item.path" :to="{ path: item.path }" :id="generateUniqueId(item)"
          :class="generateItemClass(item)">
          {{ item.label }}
        </el-breadcrumb-item>

      </el-breadcrumb>

    </div>
    <div class="r-content">
      <el-dropdown>
        <span class="el-dropdown-link">
          <img class="userIcon" :src="avatarSrc" alt="用户">
        </span>
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item @click.native="dialogAvatarVisible = true">
            修改头像
          </el-dropdown-item>
          <el-dropdown-item>
            选项二
          </el-dropdown-item>
          <el-dropdown-item @click.native="handleLogout">
            退出登录
          </el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
      <!-- 这段代码是一个使用Element UI的对话框组件（`<el-dialog>`）来修改头像的功能。下面是对其中每个属性和事件的解释：
        - `el-upload`：Element UI的上传组件，用于上传头像图片。
        - `class="avatar-uploader"`：为上传组件添加一个CSS类名，用于自定义样式。
        - `action`：指定上传的目标地址，即"http://localhost:80/sports/img/uploadAvatar"。
        - `:show-file-list="false"`：设置为`false`，不显示上传成功后的文件列表。
        - `:on-success="handleAvatarSuccess"`：上传成功后的回调函数，指定为`handleAvatarSuccess`方法。
        - `:before-upload="beforeAvatarUpload"`：上传前的钩子函数，指定为`beforeAvatarUpload`方法。
        - `:headers="{'Jwt': this.token}"`：请求头部信息，传递一个`{'Jwt': this.token}`的对象，用于身份验证。
        - `img v-if="imageUrl" :src="imageUrl" class="avatar"`：通过`v-if`判断是否有`imageUrl`，如果有则显示上传的头像图片。
        - `i v-else class="el-icon-plus avatar-uploader-icon"`：如果没有`imageUrl`，显示一个加号图标。 -->

      <el-dialog title="修改头像" :visible.sync="dialogAvatarVisible" @close="handleDialogClose">
        <avatarShowVue :initialSrc="avatarSrc" />
      </el-dialog>
    </div>
  </div>
</template>

<script>

import avatarShowVue from './avatarShow.vue';
import { mapState } from 'vuex';
import { logout } from '@/api';

export default {
  components: {
    avatarShowVue
    },
  data() {
    return {
      dialogAvatarVisible: false,
      imageUrl: '',
      initialImageSrc: '',
      avatarSrc:this.$store.state.userInfo.avatarSrc,
    }

  },
  watch: {
    '$route'(to, from) {
      // 在路由变化时触发，你可以在这里更新面包屑的样式
      // 比如根据当前路由信息 to 来设置面包屑样式
      this.updateBreadcrumbStyle(to)
    },
    showTooltip: function (newVal, oldVal) {
      // 在这里处理 showTooltip 值的变化
      console.log("newVal", newVal),
      console.log("old", oldVal)
    }
  },
  methods: {
    handleDialogClose(){
      console.log("userInfo.avatarSrc", this.$store.state.userInfo)
      this.avatarSrc=this.$store.state.userInfo.avatarSrc;
      console.log("this.avatarSrc", this.avatarSrc)
    },


    generateUniqueId(item) {
      // 生成一个基于路由项的唯一 ID，可以根据需要自定义生成逻辑
      return `breadcrumb-item-${item.path}`;
    },
    generateItemClass(item) {
      // 生成类名，例如 "special" 类名
      if (item.path === this.$route.path) {
        return 'current_bread';
      } else {
        if (this.$route.path === '/home' & item.path === '/') {
          return 'current_bread';
        }
        return '';
      }
    },
    handleMenu() {
      this.$store.commit('collapseMenu')
    },
    updateBreadcrumbStyle(to) {
      // 根据 to 对象来更新面包屑样式，这里可以根据具体需求修改样式
      // 例如，你可以设置一个数据属性，然后在模板中使用这个属性来控制面包屑样式
      if (to.path === '/home' || to.path === '/') {
        console.log(to, '/home')
        const current_bread = document.getElementById('breadcrumb-item-' + to.path)

      } else {
        console.log(to, '其他')
        const current_bread = document.getElementById('breadcrumb-item-' + to.path)

      }
      this.$store.commit('selectMenu', to)
    },
    handleLogout() {
      let userId = this.$store.state.userInfo.userId
      // 删除JWT令牌
      localStorage.removeItem('jwtToken');
      localStorage.removeItem('sportsUser');
      if (userId > 0) {
        logout().then((data) => {
          console.log("退出登录", data)


          this.$store.commit('removeUser');
          window.location.reload();

        })
      } else {

        window.location.reload();

      }



    }
  },
  // computed 是一个对象，用于定义计算属性
  computed: {
    // ...mapState 意味着将 mapState 返回的所有属性都添加到 computed 对象中。
    //创建一个名为 tags 的计算属性，它将获取 Vuex 的状态中 state.tab.tabsList 的数据。
    ...mapState({
      tags: state => state.tab.tabsList
    })
  },
  mounted() {
    console.log(this.tags, 'tags')
  
  }

}
</script>

<style lang="less" scoped>
.tooltip {
  position: absolute;
  bottom: 10px;
  left: 50%;
  transform: translateX(-50%);
  background-color: rgba(242, 14, 14, 0.5);
  color: #121111;
  padding: 4px 8px;
  border-radius: 4px;
  display: none;
}

.header-container {
  background-color: rgb(61, 151, 203);
  height: 50px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-left: 20px;

  .text {
    color: #fff;
    font-size: 14px;
    margin-left: 10px;
  }

  .r-content {
    .userIcon {
      width: 40px;
      height: 40px;
      border-radius: 50%;
    }
  }

  .l-content {
    display: flex;
    align-items: center;



    // 用于修改具有特定类名的元素的样式，这些类名是在一个包含 .el-breadcrumb__item 的深度（/deep/）选择器内的
    /deep/.el-breadcrumb__item {
      .el-breadcrumb__inner {
        // &.is-link 表示选择那些具有 .el-breadcrumb__inner 类名并且还具有 .is-link 类名的元素。& 在这里代表当前选择器，所以它表示的是 .el-breadcrumb__inner。
        font-weight: 500;

        &.is-link {
          color: #666;
          cursor: pointer;
        }
      }

      &:last-child {
        .el-breadcrumb__inner {
          color: #fff;
        }

      }
    }

    /deep/.current_bread {
      .el-breadcrumb__inner {
        color: aquamarine;
        background-color: #e90c0c;
      }
    }
  }
}

.avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.avatar-uploader .el-upload:hover {
  border-color: #409EFF;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  line-height: 178px;
  text-align: center;
}

.avatar {
  width: 178px;
  height: 178px;
  display: block;
}
</style>
