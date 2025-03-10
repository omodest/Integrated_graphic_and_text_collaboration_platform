<template>
  <div class="picture-search-form">
    <!-- 搜索表单 -->
    <a-form name="searchForm" layout="inline" :model="searchParams" @finish="doSearch">

      <a-form-item label="关键词" name="searchText">
        <a-input
          v-model:value="searchParams.searchText"
          placeholder="从名称和简介搜索"
          allow-clear
        />
      </a-form-item>

      <a-form-item name="category" label="分类">
        <a-auto-complete
          v-model:value="searchParams.category"
          style="min-width: 180px"
          placeholder="请输入分类"
          :options="categoryOptions"
          allow-clear
        />
      </a-form-item>

      <a-form-item label="标签" name="tags" >
        <a-select
          v-model:value="searchParams.tags"
          mode="tags"
          :options="tagOptions"
          placeholder="请输入标签"
          allowClear
          style="width: 180px"
        />
      </a-form-item>

      <a-form-item label="日期" name="dateRange">
        <a-range-picker
          style="width: 400px"
          show-time
          v-model:value="dateRange"
          :placeholder="['编辑开始时间', '编辑结束时间']"
          format="YYYY/MM/DD HH:mm:ss"
          :presets="rangePresets"
          @change="onRangeChange"
        />
      </a-form-item>

      <a-form-item label="名称" name="name">
        <a-input v-model:value="searchParams.name" placeholder="请输入名称" allow-clear />
      </a-form-item>

      <a-form-item label="简介" name="introduction">
        <a-input v-model:value="searchParams.introduction" placeholder="请输入简介" allow-clear />
      </a-form-item>

      <a-form-item label="宽度" name="picWidth">
        <a-input-number v-model:value="searchParams.picWidth" />
      </a-form-item>

      <a-form-item label="高度" name="picHeight">
        <a-input-number v-model:value="searchParams.picHeight" />
      </a-form-item>

      <a-form-item label="格式" name="picFormat">
        <a-input v-model:value="searchParams.picFormat" placeholder="请输入格式" allow-clear />
      </a-form-item>

      <a-form-item>
        <a-space>
          <a-button type="primary" html-type="submit" style="width: 96px">搜索</a-button>
          <a-button html-type="reset" @click="doClear">重置</a-button>
        </a-space>
      </a-form-item>

    </a-form>
  </div>
</template>

<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import {queryTagsUsingPost} from "@/api/pictureTagsRelationController";
import {queryCategoryUsingPost} from "@/api/pictureCategoryController";

// 获取父页面传递的查询参数
const props = defineProps<Props>()
interface Props {
  onSearch?: (searchParams: API.PictureQueryRequest) => void
}

// 搜索条件
const searchParams = reactive<API.PictureQueryRequest>({})

// 搜索数据
const doSearch = () => {
  props.onSearch?.(searchParams)
}

// 标签和分类选项
const categoryOptions = ref<string[]>([])
const tagOptions = ref<string[]>([])

onMounted(() => {
  getTagCategoryOptions()
})

/**
 * 1. 获取标签和分类选项
 */
const getTagCategoryOptions = async () => {

  const res = await queryTagsUsingPost()
  const resCategory = await queryCategoryUsingPost()

  if (res.data.code === 0 && res.data.data) {
    tagOptions.value = (res.data.data ?? []).map((data: string) => {
      return {
        value: data?.tagName,
        label: data?.tagName,
      }
    })

    categoryOptions.value = (resCategory.data.data ?? []).map((data: string) => {
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
 * 2. 日期范围更改时触发
 * @param dates
 * @param dateStrings
 */
const dateRange = ref<[]>([])
const onRangeChange = (dates: any[], dateStrings: string[]) => {
  if (dates?.length >= 2) {
    searchParams.startEditTime = dates[0].toDate()
    searchParams.endEditTime = dates[1].toDate()
  } else {
    searchParams.startEditTime = undefined
    searchParams.endEditTime = undefined
  }
}

// 3. 时间范围预设
const rangePresets = ref([
  { label: '过去 7 天', value: [dayjs().add(-7, 'd'), dayjs()] },
  { label: '过去 14 天', value: [dayjs().add(-14, 'd'), dayjs()] },
  { label: '过去 30 天', value: [dayjs().add(-30, 'd'), dayjs()] },
  { label: '过去 90 天', value: [dayjs().add(-90, 'd'), dayjs()] },
])

// 4. 清理
const doClear = () => {
  // 取消所有对象的值
  Object.keys(searchParams).forEach((key) => {
    searchParams[key] = undefined
  })
  // 日期筛选项单独清空，必须定义为空数组
  dateRange.value = []
  // 清空后重新搜索
  props.onSearch?.(searchParams)
}
</script>
<style scoped>
.picture-search-form .ant-form-item {
  margin-top: 16px;
}
</style>
