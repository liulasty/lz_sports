<template>
  <div>
    <img :src="imageSrc" class="avatarSrc" alt="展示的图片" />
    <label for="uploadBtn" class="customBtn" style="margin: 20px;"> <el-button id="uploadBtn1" type="primary"
        @click="handleUpload">上传图片</el-button></label>
    <input type="file" id="uploadBtn" @change="uploadImage" accept="image/*" style="display: none;">
    <div class="imageUploadItem">
      <p>请选择图片文件：只允许上传图片文件，支持格式包括 JPG、PNG、GIF 等常见图片格式。</p>
      <p>图片大小限制：上传的图片文件大小不得超过 5MB。</p>
      <p>图片清晰度：请确保上传的图片清晰，避免模糊或失真的情况。</p>
      <p>建议尺寸：建议使用正方形或接近正方形的图片，以获得最佳效果。</p>
    </div>
  </div>
</template>

<script>

import { uploadAvatar } from '@/api'
export default {
  props: {
    initialSrc: {
      type: String,
      required: true,
    },
  },
  data() {
    return {
      imageSrc: this.initialSrc, // 初始展示的图片,
      isUpload: 1,
    };
  },
  methods: {
    uploadImage(event) {
      const file = event.target.files[0];

      console.log("上传头像结果", this.isUpload);
      if (file) {
        // 验证图片大小
        if (file.size > 5 * 1024 * 1024) {
          this.$message.error('图片大小不能超过5MB');
          this.isUpload = 0;
          console.log("上传头像结果1", this.isUpload);
          return;

        }

        // 验证图片格式
        const allowedExtensions = /(\.jpg|\.jpeg|\.png|\.gif)$/i;
        if (!allowedExtensions.exec(file.name)) {
          this.$message.error('只允许上传JPG、PNG、GIF格式的图片');
          this.isUpload = 0;
          console.log("上传头像结果2", this.isUpload);
          return;
        }

        // 验证图片尺寸,此处仅作示例,您可以根据实际需求自定义验证逻辑
        const img = new Image();
        img.src = URL.createObjectURL(file);
        img.onload = () => {
          const width = img.width;
          const height = img.height;
          if (width !== height) {
            this.$message.error('建议使用正方形或接近正方形的图片');
            this.isUpload = 0;
            console.log("上传头像结果3", this.isUpload);
            return;
          }
        };
        // if (this.isUpload === 1) {
        //   uploadAvatar(file).then((result) => {
        //     console.log("上传头像结果", result);
        //     if (result.data.code != 0) {
        //       this.$notify({
        //         title: '修改结果',
        //         message: result.data.msg,
        //         type: 'success'
        //       });
        //       this.imageSrc = "https://" + result.data.data;
        //       // 在组件中调用 mutations 中的方法
        //       this.$store.commit('updatedAvatarSrc', this.imageSrc);
        //       console.log("上传头像后", this.$store.state.userInfo);
        //     } else {
        //       this.$notify.error({
        //         title: '修改失败',
        //         message: result.data.msg
        //       });
        //     }
        //   })
        // }
      }
      console.log("上传头像结果4", this.isUpload);

    },
    handleUpload() {
      const uploadBtn = document.getElementById('uploadBtn');
      uploadBtn.click();
    },
  },
};
</script>

<style lang="less" scoped>
.avatarSrc {
  height: 400px;
  width: 400px;
}

.imageUploadItem p {
  margin-bottom: 10px;
  line-height: 1.5;
}

.imageUploadItem {
  position: absolute;
  top: 150px;
  width: 300px;
  right: 20px;
  background-color: #333;
  color: #fff;
  padding: 5px 10px;
  border-radius: 5px;
}
</style>