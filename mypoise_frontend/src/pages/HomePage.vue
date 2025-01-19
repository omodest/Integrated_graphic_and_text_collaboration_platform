<template>
  <div id="homePage">
    <h1>{{ msg }}</h1>
    <!-- 搜索框 -->
    <div class="search-bar">
      <a-input-search
        v-model:value="searchParams.searchText"
        placeholder="从海量图片中搜索"
        enter-button="搜索"
        size="large"
        @search="doSearch"
      />
    </div>
    <!-- 分类和标签筛选 -->
    <a-tabs v-model:active-key="selectedCategory" @change="doSearch">
      <a-tab-pane key="all" tab="全部" />
      <a-tab-pane v-for="category in categoryList" :tab="category.categoryName" :key="category.id" />
    </a-tabs>

    <div class="tag-bar">
      <span style="margin-right: 8px">标签：</span>
      <a-space :size="[0, 8]" wrap>
        <a-checkable-tag
          v-for="(tag, index) in tagList"
          :key="tag"
          v-model:checked="selectedTagList[index]"
          @change="doSearch"
          :class="{'hot-tag': hotList.includes(tag.tagName)}"
        >
          {{ tag.tagName }}
        </a-checkable-tag>
      </a-space>
    </div>

    <!-- 图片列表 -->
    <a-list
      :grid="{ gutter: 16, xs: 1, sm: 2, md: 3, lg: 4, xl: 5, xxl: 6 }"
      :data-source="dataList"
      :pagination="pagination"
      :loading="loading"
    >
      <template #renderItem="{ item: picture }">
        <a-list-item style="padding: 0">
          <!-- 单张图片 -->
          <a-card hoverable @click="doClickPicture(picture)">

            <template #cover>
              <img
                :alt="picture.name"
                :src="picture.url"
                style="height: 180px; object-fit: cover"
              />
            </template>
            <!-- 图片详情信息           -->
<!--            {{picture}}-->
            <a-card-meta :title="picture.name">
              <template #description>
                <a-flex>
                  <a-tag color="green">
                    {{ picture.category ?? '默认' }}
                  </a-tag>
                  <a-tag v-for="tag in picture.tagNames" :key="tag">
                    {{ tag }}
                  </a-tag>
                </a-flex>
              </template>
            </a-card-meta>

          </a-card>
        </a-list-item>
      </template>
    </a-list>

  </div>
</template>

<script setup lang="ts">
import {getHotTagsUsingGet, queryTagsUsingPost} from "@/api/pictureTagsRelationController";

const msg = "这里是本平台的主页~";
import { computed, onMounted, reactive, ref } from 'vue'
import {
  listPictureVoByPageUsingPost,
} from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import {queryCategoryUsingPost} from "@/api/pictureCategoryController";

const router = useRouter()
// 定义数据
const dataList = ref<API.PictureVO[]>([])
const total = ref(0)
const loading = ref(true)

// 标签和分类列表
const categoryList = ref<string[]>([])
const selectedCategory = ref<string>('all')
const tagList = ref<string[]>([])
const selectedTagList = ref<boolean[]>([])

// 搜索条件
const searchParams = reactive<API.PictureQueryRequest>({
  current: 1,
  pageSize: 12,
  sortField: 'createTime',
  sortOrder: 'descend',
} as API.PictureQueryRequest)

// 分页参数
const pagination = computed(() => {
  return {
    current: searchParams.current,
    pageSize: searchParams.pageSize,
    total: total.value,
    onChange: (page: number, pageSize: number) => {
      searchParams.current = page
      searchParams.pageSize = pageSize
      fetchData()
    },
  }
})

// 页面加载时获取数据，请求一次
onMounted(() => {
  fetchData()
  getHotTag()
})
onMounted(() => {
  getTagCategoryOptions()
})

// 搜索
const doSearch = () => {
  // 重置搜索条件
  searchParams.current = 1
  fetchData()
}

// 1. 获取数据
const fetchData = async () => {
  loading.value = true
  // 转换搜索参数
  const params = {
    ...searchParams,
    tags: [] as string[],
  }
  // 如果不是全部，就表示需要筛选了
  if (selectedCategory.value !== 'all') {
    params.category = selectedCategory.value
  }
  // [true, false, false] => ['java']
  // 将选中的标签设置为true，否则为空
  selectedTagList.value.forEach((useTag, index) => {
    if (useTag) {
      // console.log(tagList.value[index].tagName)
      params.tags.push(tagList.value[index].tagName)
    }
  })

  const res = await listPictureVoByPageUsingPost(params)
  if (res.data.code === 0 && res.data.data ) {
    dataList.value = res.data.data.records ?? []
    total.value = res.data.data.total ?? 0
  }else if (res.data.code === 0 && res.data.data === null){
    dataList.value =  []
    total.value = 0
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
  loading.value = false
}

/**
 * 2. 获取标签和分类选项
 */
const getTagCategoryOptions = async () => {

  const res = await queryTagsUsingPost()
  const res1 = await queryCategoryUsingPost()

  if (res.data.code === 0 && res.data.data) {
    tagList.value = res.data.data ?? []
    categoryList.value = res1.data.data ?? []
  } else {
    message.error('获取标签分类列表失败，' + res.data.message)
  }
}

/**
 * 3. 跳转至图片详情页
 */
const doClickPicture = (picture: API.PictureVO) => {
  router.push({
    path: `/picture/${picture.id}`,
  })
}

/**
 * 获取热门标签
 */
let hotList = []
const getHotTag = async () => {
  const response = await getHotTagsUsingGet();
  if (response.data.code === 0){
    hotList = response.data.data.map(tag => tag.tagName);
  }
}

</script>

<style scoped>
#homePage {
  margin-bottom: 16px;
}
#homePage .search-bar {
  max-width: 480px;
  margin: 0 auto 16px;
}
#homePage .tag-bar {
  margin-bottom: 16px;
}
.hot-tag {
  text-decoration: underline;
  color: #ff4081;
  font-weight: bold;
  font-size: 14px;
}


</style>
