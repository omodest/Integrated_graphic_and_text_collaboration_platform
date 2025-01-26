<template>
  <div id="addSpacePage">
    <h2 style="margin-bottom: 16px">
<!--      {{ route.query?.id ? '修改空间' : '创建空间' }}-->
      {{ route.query?.id ? '修改' : '创建' }} {{ SPACE_TYPE_MAP[spaceType] }}
    </h2>

    <!-- 空间信息表单 -->
    <a-form name="spaceForm" layout="vertical" :model="spaceForm" @finish="handleSubmit">

      <a-form-item name="spaceName" label="空间名称">
        <a-input v-model:value="spaceForm.spaceName" placeholder="请输入空间" allow-clear />
      </a-form-item>

      <a-form-item name="spaceLevel" label="空间级别">
        <a-select
          v-model:value="spaceForm.spaceLevel"
          style="min-width: 180px"
          placeholder="请选择空间级别"
          :options="SPACE_LEVEL_OPTIONS"
          allow-clear
        />
      </a-form-item>

      <div v-if="route.query?.id">
        <a-form-item name="spaceName" label="空间大小">
          <a-input v-model:value="spaceForm.maxSize" placeholder="请输入空间大小(以B为单位)" allow-clear />
        </a-form-item>

        <a-form-item name="spaceName" label="空间条数">
          <a-input v-model:value="spaceForm.maxCount" placeholder="请输入空间条数" allow-clear />
        </a-form-item>
      </div>

      <a-form-item>
        <a-button type="primary" html-type="submit" :loading="loading" style="width: 100%">
          提交
        </a-button>
      </a-form-item>

    </a-form>
    <!-- 空间级别介绍 -->
    <a-card title="空间级别介绍">

      <a-typography-paragraph>
        * 目前仅支持开通普通版，如需升级空间请联系管理员
      </a-typography-paragraph>

      <a-typography-paragraph v-for="spaceLevel in spaceLevelList">
        {{ spaceLevel.text }}：大小 {{ formatSize(spaceLevel.maxSize) }}，上传数量
        {{ spaceLevel.maxCount }}
      </a-typography-paragraph>

    </a-card>
  </div>
</template>
<script setup lang="ts">
import {computed, onMounted, reactive, ref} from 'vue'
import { message } from 'ant-design-vue'
import {
  addSpaceUsingPost,
  getSpaceVoByIdUsingGet,
  listSpaceLevelUsingGet,
  updateSpaceUsingPost,
} from '@/api/spaceController.ts'
import { useRoute, useRouter } from 'vue-router'
import {SPACE_LEVEL_OPTIONS, SPACE_TYPE_ENUM} from '@/constant/space.ts'
import { formatSize } from '@/utils'
import { SPACE_TYPE_MAP } from "@/constant/space";

const loading = ref(false)
// 获取URL
const route = useRoute()
// 进行页面跳转
const router = useRouter()
// 定义数据
const space = ref<API.SpaceVO>()
const spaceLevelList = ref<API.SpaceLevel[]>([])
// 空间表单
const spaceForm = reactive<API.SpaceAddRequest | API.SpaceUpdateRequest>({})

// 1. 页面加载，获取空间级别；编辑-加载老数据
onMounted(() => {
  fetchSpaceLevelList()
})
onMounted(() => {
  getOldSpace()
})

// 2. 获取空间级别
const fetchSpaceLevelList = async () => {
  const res = await listSpaceLevelUsingGet()
  if (res.data.code === 0 && res.data.data) {
    spaceLevelList.value = res.data.data
  } else {
    message.error('获取空间级别失败，' + res.data.message)
  }
}

/**
 * 3. 提交表单
 * @param values
 */
const handleSubmit = async (values: any) => {
  const spaceId = space.value?.id
  loading.value = true
  let res
  if (spaceId) {
    // 更新
    res = await updateSpaceUsingPost({
      id: spaceId,
      ...spaceForm,
    })
  } else {
    // 创建
    res = await addSpaceUsingPost({
      ...spaceForm,
      spaceType: spaceType.value,
    })
  }
  // 操作成功
  if (res.data.code === 0 && res.data.data) {
    message.success('操作成功')
    // 跳转到空间详情页
    await router.push({
      path: `/admin/spaceManage`,
    })
  } else {
    message.error('操作失败，' + res.data.message)
  }
  loading.value = false
}

// 4. 获取老数据
const getOldSpace = async () => {
  // 获取到 id
  const id = route.query?.id
  if (id) {
    const res = await getSpaceVoByIdUsingGet({
      id,
    }as API.getSpaceByIdUsingGETParams)
    if (res.data.code === 0 && res.data.data) {
      const data = res.data.data
      space.value = data
      // 填充表单
      spaceForm.spaceName = data.spaceName
      spaceForm.spaceLevel = data.spaceLevel
      spaceForm.maxSize = data.maxSize
      spaceForm.maxCount = data.maxCount
    }
  }
}

// 5.
// 空间类别，默认为私有空间
const spaceType = computed(() => {
  if (route.query?.type) {
    return Number(route.query.type)
  } else {
    return SPACE_TYPE_ENUM.PRIVATE
  }
})
</script>
<style scoped>
#addSpacePage {
  max-width: 720px;
  margin: 0 auto;
}
</style>
