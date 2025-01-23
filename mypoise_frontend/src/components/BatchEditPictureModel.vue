<template>
  <div class="batch-edit-picture-modal">

    <a-modal v-model:visible="visible" title="批量编辑图片" :footer="false" @cancel="closeModal">

      <a-typography-paragraph type="secondary">* 只对当前页面的图片生效</a-typography-paragraph>

      <!-- 批量创建表单 -->
      <a-form name="formData" layout="vertical" :model="formData" @finish="handleSubmit">

        <a-form-item name="category" label="分类">
          <a-auto-complete
            v-model:value="formData.category"
            placeholder="请输入分类"
            :options="categoryOptions"
            allow-clear
          />
        </a-form-item>

        <a-form-item name="tags" label="标签">
          <a-select
            v-model:value="formData.tags"
            mode="tags"
            placeholder="请输入标签"
            :options="tagOptions"
            allow-clear
          />
        </a-form-item>

        <a-form-item name="nameRule" label="命名规则">
          <a-input
            v-model:value="formData.nameRule"
            placeholder="请输入命名规则，输入 {序号} 可动态生成"
            allow-clear
          />
        </a-form-item>

        <a-form-item>
          <a-button type="primary" html-type="submit" style="width: 100%">提交</a-button>
        </a-form-item>

      </a-form>
    </a-modal>
  </div>
</template>
<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue'
import {
  editPictureByBatchUsingPost,
} from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'
import {queryTagsUsingPost} from "@/api/pictureTagsRelationController";
import {queryCategoryUsingPost} from "@/api/pictureCategoryController";

// 接收父组件的传值
const props = withDefaults(defineProps<Props>(), {})
interface Props {
  pictureList: API.PictureVO[]
  spaceId: number
  onSuccess: () => void
}

// 是否可见
const visible = ref(false)

// 打开弹窗
const openModal = () => {
  visible.value = true
}

// 关闭弹窗
const closeModal = () => {
  visible.value = false
}

// 暴露函数给父组件
defineExpose({
  openModal,
})

onMounted(() => {
  getTagCategoryOptions()
})

// 填写表单
const formData = reactive<API.PictureEditByBatchRequest>({
  category: '',
  tags: [],
  nameRule: '',
})

// 标签分类数据下拉框
const categoryOptions = ref<string[]>([])
const tagOptions = ref<string[]>([])

/**
 * 1. 获取标签和分类选项
 * @param values
 */
const getTagCategoryOptions = async () => {

  const res = await queryTagsUsingPost()
  const res1 = await queryCategoryUsingPost()

  if (res.data.code === 0 && res.data.data) {
    tagOptions.value = (res.data.data ?? []).map((data: string) => {
      return {
        value: data?.tagName,
        label: data?.tagName,
      }
    })

    categoryOptions.value = (res1.data.data ?? []).map((data: string) => {
      return {
        value: data?.categoryName,
        label: data?.categoryName,
      }
    })
  } else {
    message.error('获取标签分类列表失败，' + res.data.message)
  }
}

/**
 * 2. 提交表单
 * @param values
 */
const handleSubmit = async (values: any) => {
  if (!props.pictureList) {
    return
  }
  const res = await editPictureByBatchUsingPost({
    pictureIdList: props.pictureList.map((picture) => picture.id),
    spaceId: props.spaceId,
    ...values,
  })
  // 操作成功
  if (res.data.code === 0 && res.data.data) {
    message.success('操作成功')
    closeModal()
    props.onSuccess?.()
  } else {
    message.error('操作失败，' + res.data.message)
  }
}
</script>
