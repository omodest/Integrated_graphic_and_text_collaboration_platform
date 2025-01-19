<template>
  <div class="picture-upload">
    <!-- 上传图片 :custom-request自定义上传函数，调用后端接口；:show-upload-list是否展示图片列表；  -->
    <a-upload
      list-type="picture-card"
      :show-upload-list="false"
      :custom-request="handleUpload"
      :before-upload="beforeUpload"
    >
      <!-- 展示上传的图片     -->
      <img v-if="picture?.url" :src="picture?.url" alt="avatar" />
      <!-- 展示空的上传框     -->
      <div v-else>
        <loading-outlined v-if="loading"></loading-outlined>
        <plus-outlined v-else></plus-outlined>
        <div class="ant-upload-text">点击或拖拽上传图片</div>
      </div>
    </a-upload>
  </div>
</template>

<script setup lang="ts">
import {message, UploadProps} from "ant-design-vue";
import {LoadingOutlined, PlusOutlined} from "@ant-design/icons-vue";
import {ref} from "vue";
import {uploadPictureUsingPost} from "@/api/fileController";

// 加载样式
const loading = ref<boolean>(false)
const props = defineProps<Props>();
/**
 * 拿到父组页面的值
 */
interface Props{
  picture?: API.PictureVO;
  onSuccess: (newPicture: API.PictureVO) => void;
}

/*
* 1. 上传前校验
* @param file
*/
const beforeUpload = (file: UploadProps['fileList'][number]) => {
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png'
  if (!isJpgOrPng) {
    message.error('不支持上传该格式的图片，推荐 jpg 或 png')
  }
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    message.error('不能上传超过 2M 的图片')
  }
  return isJpgOrPng && isLt2M
}

/**
 * 2. 上传
 * @param file
 */
const handleUpload = async ({ file }: any) => {
  loading.value = true
  try {
    // 标志,判断上传还是更新
    const params: API.PictureUpdateRequest = props.picture ? { id: props.picture.id } : {};
    const res = await uploadPictureUsingPost(params, {}, file)
    if (res.data.code === 0 && res.data.data) {
      message.success('图片上传成功')
      // 将上传成功的图片信息传递给父组件
      props.onSuccess?.(res.data.data)
    } else {
      message.error('图片上传失败，' + res.data.message)
    }
  } catch (error) {
    message.error('图片上传失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/**
 :deep 修改upload内置样式
 */
.picture-upload :deep(.ant-upload) {
  width: 100% !important;
  height: 100% !important;
  min-height: 152px;
  min-width: 152px;
}

.picture-upload img {
  max-width: 100%;
  max-height: 480px;
}

.ant-upload-select-picture-card i {
  font-size: 32px;
  color: #999;
}

.ant-upload-select-picture-card .ant-upload-text {
  margin-top: 8px;
  color: #666;
}
</style>
