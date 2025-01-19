<template>
  <div id="addPicturePage">
    <h2 style="margin-bottom: 16px">
      {{ route.query?.id ? '修改图片' : '创建图片' }}
    </h2>
    <!-- 图片上传组件   -->
    <PictureUpload :picture="picture" :onSuccess="onSuccess" />
    <!-- 输入表单   -->
    <a-form  v-if="picture" layout="vertical" :model="pictureForm" @finish="handleSubmit">
      <a-form-item label="名称" name="name">
        <a-input v-model:value="pictureForm.name" placeholder="请输入名称" />
      </a-form-item>

      <a-form-item label="简介" name="introduction">
        <a-textarea
          v-model:value="pictureForm.introduction"
          placeholder="请输入简介"
          :rows="2"
          :auto-size="{minRows: 2, maxRows: 5}"
          allowClear
        />
      </a-form-item>

      <a-form-item label="分类" name="category">
        <a-auto-complete
          v-model:value="pictureForm.category"
          placeholder="请输入分类"
          :options="categoryOptions"
          allowClear
        />
      </a-form-item>

      <a-form-item label="标签" name="tags">
        <a-select
          v-model:value="pictureForm.tags"
          mode="tags"
          :options="tagOptions"
          placeholder="请输入标签"
          allowClear
        />
      </a-form-item>

      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100%">创建</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>
<script setup lang="ts">
import PictureUpload from "@/components/PictureUpload.vue";
import {onMounted, reactive, ref} from "vue";
import {editPictureUsingPost, getPictureVoByIdUsingGet} from "@/api/pictureController";
import {message} from "ant-design-vue";
import {useRoute, useRouter} from "vue-router";
import {queryCategoryUsingPost} from "@/api/pictureCategoryController";
import {queryTagsUsingPost} from "@/api/pictureTagsRelationController";

const route = useRoute() // 用来获取路径
const router = useRouter() // 用来实现页面跳转
// 上传的图片
const picture = ref<API.PictureVO>()
// 图片编辑表单
const pictureForm = reactive<API.PictureEditRequest>({})
// 分类列表
const categoryOptions = ref<string[]>([])
// 标签列表
const tagOptions = ref<string[]>([])


/**
 * 1. 上传图片后，可以将得到的图片信息（比如名称）填充到表单
 * 一开始上传的图片会自带一个图片名称，所以这里将图片名称和图片在页面中渲染出来
 */
const onSuccess = (newPicture: API.PictureVO) => {
  picture.value = newPicture
  pictureForm.name = newPicture.name
}

/**
 * 2. 提交表单
 * @param values
 */
const handleSubmit = async (values: any) => {
  // 图片编辑，拿到图片id
  const pictureId = picture.value?.id
  if (!pictureId) {
    return
  }
  console.log(values)
  const res = await editPictureUsingPost({
    id: pictureId,
    ...values,
  })
  if (res.data.code === 0 && res.data.data) {
    message.success('创建成功')
    // 跳转到图片详情页
    await router.push({
      path: `/picture/${pictureId}`,
    })
  } else {
    message.error('创建失败，' + res.data.message)
  }
}

// 3. 获取标签和分类选项
const getTagCategoryOptions = async () => {
  const resCategory = await queryCategoryUsingPost()
  const resTags = await queryTagsUsingPost();
  if (resCategory.data.code === 0 && resCategory.data.data) {
    // 转换成下拉选项组件接受的格式
    tagOptions.value = (resTags.data.data ?? []).map((data: string) => {
      return {
        value: data?.tagName,
        label: data?.tagName,
      }
    })
  }
  if (resCategory.data.code === 0 && resCategory.data.data) {
    categoryOptions.value = (resCategory.data.data ?? []).map((data: string) => {
      return {
        value: data?.categoryName,
        label: data?.categoryName,
      }
    })
  }
}

/**
 * 4. 页面初始化执行的函数
 */
onMounted(() => {
  getTagCategoryOptions()
  getOldPicture() // 判断是否是编辑
})

/**
 * 5. 获取老数据
*/
const getOldPicture = async () => {
  // 获取数据
  const id = route.query?.id

  if (id) {
    // 如果id不为空，获取老数据
    const res = await getPictureVoByIdUsingGet({
      id: id,
    } as API.getPictureVOByIdUsingGETParams)
    console.log(res)
    if (res.data.code === 0 && res.data.data) {
      const data = res.data.data
      console.log(data)
      picture.value = data
      pictureForm.name = data.name
      pictureForm.introduction = data.introduction
      pictureForm.category = data.category
      pictureForm.tags = data.tagNames
    }
  }
}

</script>

<style scoped>
#addPicturePage {
  max-width: 720px;
  margin: 0 auto;
}
</style>
