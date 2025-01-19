<template>
  <div id="categoryManagePage">
    <!-- 表格 -->
    <a-table
      :columns="columns"
      :data-source="dataList"
      :pagination="pagination"
      @change="doTableChange"
      row-key="id"
      :editable="true"
    >
      <!-- 一些特殊的字段需要渲染不同的样式     -->
      <template #bodyCell="{ column, record, index }">
        <template v-if="column.key === 'action'">
          <a-button danger @click="doDelete(record.id)">删除</a-button>
        </template>
      </template>
    </a-table>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import {
  createUserUsingPost,
  deleteUserUsingPost,
  getListUserVoByPageUsingPost,
  updateUserUsingPost
} from '@/api/userController.ts'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import {
  addCategoryUsingPost,
  delCategoryUsingPost,
  editCategoryUsingPost,
  queryCategoryUsingPost
} from "@/api/pictureCategoryController";
import {delTagsUsingPost, queryTagsUsingPost} from "@/api/pictureTagsRelationController";

// 定义数据列表
const dataList = ref<API.PictureTags[]>([])
// 总条数
const total = ref(0)
// 搜索条件
const searchParams = reactive({
  current: 1,
  pageSize: 5,
})

// 分页参数
const pagination = computed(() => {
  return {
    current: searchParams.current,
    pageSize: searchParams.pageSize,
    total: total.value,
    showSizeChanger: true,
    showTotal: (total) => `共 ${total} 条`,
  }
})

// 搜索数据
const doSearch = () => {
  // 重置页码
  searchParams.current = 1
  fetchData()
}

// 页面加载时获取数据，请求一次
onMounted(() => {
  fetchData()
})

// 表格变化之后，重新获取数据
const doTableChange = (page: any) => {
  searchParams.current = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

// 1. 获取数据
const fetchData = async () => {
  const res = await queryTagsUsingPost({
    ...searchParams,
  })
  if (res.data.code === 0 && res.data.data) {
    dataList.value = res.data.data ?? []
    total.value = res.data.data.length ?? 0
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
}

// 2. 删除数据
const doDelete = async (id: number) => {
  if (!id) {
    return
  }
  console.log(id)
  const res = await delTagsUsingPost(id)
  if (res.data.code === 0) {
    message.success('删除成功')
    // 刷新数据
    await fetchData()
  } else {
    message.error('删除失败')
  }
}

/**
 * 定义数据列
 */
const columns = [
  {
    title: 'id',
    dataIndex: 'id',
  },
  {
    title: '使用量',
    dataIndex: 'applyTotal',
  },
  {
    title: '分类名称',
    dataIndex: 'tagName',
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
  },
  {
    title: '操作',
    key: 'action',
  },
]
</script>
