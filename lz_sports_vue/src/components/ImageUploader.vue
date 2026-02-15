<template>
    <div>
        <!-- 文件选择输入框 -->
        <input type="file" ref="fileInput" @change="handleFileChange_new" multiple>

        <!-- <button @click="addImage">添加图片</button> -->
        <div class="image-container">
            <div v-for="(image, index) in imageUrls" :key="index" class="image-item">
                <img :src="image.url" alt="上传的图片" />
                <button class="delete-button" @click.prevent="removeImage(index)">删除</button>
            </div>
        </div>

        <!-- 触发上传按钮 -->
        <!-- <button @click="uploadImages">上传图片</button> -->

    </div>
</template>
  
<script>
import { deleteImg } from '@/api'

export default {

    data() {
        return {
            //图片的集合
            imageUrls: [],
            //需要删除的图片
            deleteImagesUrls: [],
            //原有的图片
            OriginalPhotos: [],
            data: '初始数据'
        };
    },
    mounted() {

    },
    methods: {
        handleFileChange_new() {
            // 通过文件选择输入框获取用户选择的文件
            const selectedFiles = this.$refs.fileInput.files;

            // 使用FileReader读取图片并将其存储在images数组中
            for (let i = 0; i < selectedFiles.length; i++) {
                const reader = new FileReader();
                reader.onload = (e) => {
                    this.imageUrls.push({ url: e.target.result, type: 'new', ossUrl: 'null' });
                };
                reader.readAsDataURL(selectedFiles[i]);
            }
        },
        addImage(url) {
            // 在这里可以通过某个方法获取新的图片URL并添加到集合中
            const newImageUrl = "http://image-upload-and-management.oss-cn-beijing.aliyuncs.com/" + url;
            this.loadImageAsBase64(newImageUrl)
                .then((base64String) => {
                    // console.log(base64String);
                    const imageUrl = "data:image/jpeg;base64," + base64String;
                    this.imageUrls.push({ url: imageUrl, type: 'old', ossUrl: newImageUrl });
                })
                .catch((error) => {
                    console.error(error);
                });
        },
        removeImage(index) {
            if (this.imageUrls[index].type == 'old') {
                this.deleteImagesUrls.push(this.imageUrls[index]);
            }
            this.imageUrls.splice(index, 1);
        },
        uploadImages() {
            this.deleteImage();
            for (let i = 0; i < this.imageUrls.length; i++) {
                const image = this.imageUrls[i];

                if (image.type === 'old') {
                    continue;
                }

                const formData = new FormData();
                formData.append('file', this.dataURLtoFile(image.url, `image${i}.png`));


                const xhr = new XMLHttpRequest();
                xhr.open('POST', 'http://localhost:80/sports/img/upload', false); // 设置请求为同步
                xhr.send(formData);

                if (xhr.status === 200) {
                    console.log('文件上传成功！', xhr.response);
                    image.ossUrl = xhr.responseText
                } else {
                    console.error('文件上传失败！错误码：' + xhr.status);
                }

                this.imageUrls[i].ossUrl = image.ossUrl
                console.log("修改的阿里云图片", this.imageUrls[i])
            }

        },
        deleteImage() {
            for (let i = 0; i < this.deleteImagesUrls.length; i++) {
                const image = this.deleteImagesUrls[i];
                const formData = new FormData();
                formData.append('deleteImagesUrl', image.ossUrl);
                deleteImg(formData).then((response) => {
                    // 处理成功响应，这里可以根据需要自定义逻辑，例如将图片URL添加到图片URL数组中
                    console.log("上传图片的响应", response.data)
                    // this.uploadedImages.push(response.data);
                })
                    .catch((error) => {
                        // 处理错误响应，这里可以根据需要自定义逻辑，例如显示错误信息给用户
                        console.error(error);
                    });
            }

        },
        dataURLtoFile(dataURL, fileName) {
            const arr = dataURL.split(',');
            // 匹配以冒号（:）开头、分号（;）结尾的文本片段
            const mime = arr[0].match(/:(.*?);/)[1];
            const bstr = atob(arr[1]);
            let n = bstr.length;
            const u8arr = new Uint8Array(n);
            while (n--) {
                u8arr[n] = bstr.charCodeAt(n);
            }
            return new File([u8arr], fileName, { type: mime });
        },
        loadImageAsBase64(url) {
            return new Promise((resolve, reject) => {
                fetch(url)
                    .then(response => response.blob())
                    .then(blob => {
                        const reader = new FileReader();
                        reader.onloadend = () => {
                            resolve(reader.result.split(',')[1]);
                        };
                        reader.readAsDataURL(blob);
                    })
            });
        },
        //发送总共的集合
        sendImgCollection() {
            const collection = [];

            for (let index = 0; index < this.imageUrls.length; index++) {
                const image = this.imageUrls[index].oss_url;
                collection.push(image)
            }

            fetch('http://localhost:80/imageList', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(collection),
            })
                .then(response => {
                    if (response.ok) {
                        // 请求成功，可以在这里处理成功的逻辑
                        console.log('集合发送成功');
                    } else {
                        // 请求失败，可以在这里处理失败的逻辑
                        console.log('集合发送失败');
                    }
                })
                .catch(error => {
                    // 请求过程中发生错误，可以在这里处理错误
                    console.log('请求发生错误:', error);
                });
        },
        //测试组件间通信
        testImg() {
            console.log('testIMg')
        },
        
        initImgSet(){
            console.log("清理图片集合")
            //图片的集合
            this.imageUrls = []
            //需要删除的图片
            this.deleteImagesUrls = []
            //原有的图片
            this.OriginalPhotos = []
            this.$refs.fileInput.value = null
        }

    },
};
</script>
<style scoped>
.image-container {
    display: flex;
    flex-wrap: wrap;
}

.image-item {
    margin: 10px;
    display: inline-block;
    border: 1px solid #ccc;
    padding: 10px;
    text-align: center;
    background-color: #f9f9f9;
    box-shadow: 0px 2px 4px rgba(0, 0, 0, 0.1);
    position: relative;
    /* 添加相对定位 */
}

.image-item img {
    max-width: 100%;
    max-height: 200px;
    margin-bottom: 10px;
}

.delete-button {
    background-color: #ff5555;
    color: white;
    border: none;
    padding: 5px 10px;
    cursor: pointer;
    transition: background-color 0.3s;
    position: absolute;
    /* 添加绝对定位 */
    top: 10px;
    /* 距离顶部的距离 */
    right: 10px;
    /* 距离右侧的距离 */
    opacity: 0;
    /* 初始时隐藏按钮 */
}

.image-item:hover .delete-button {
    opacity: 1;
    /* 鼠标悬停时显示按钮 */
}
</style>
  
  