<template>
  <div id="spaceDetailPage">
    <!-- 空间信息 -->
    <a-flex justify="space-between">
      <h2>{{ space.spaceName }}（{{ SPACE_TYPE_MAP[space.spaceType] }}）</h2>

      <a-space size="middle">

        <a-button
          v-if="canUploadPicture"
          type="primary"
          :href="`/add_picture?spaceId=${id}`"
          target="_blank"
        >
          + 创建图片
        </a-button>
        <a-button
          v-if="needHiddenUserManage"
          type="primary"
          ghost
          :icon="h(TeamOutlined)"
          :href="`/spaceUserManage/${id}`"
          target="_blank"
        >
          成员管理
        </a-button>

        <a-button
          v-if="canManageSpaceUser"
          type="primary"
          ghost
          :icon="h(BarChartOutlined)"
          :href="`/space_analyze?spaceId=${id}`"
          target="_self"
        >
          空间分析
        </a-button>

        <a-button v-if="canEditPicture" :icon="h(EditOutlined)" @click="doBatchEdit"> 批量编辑</a-button>

        <a-tooltip
          :title="`占用空间 ${formatSize(space.totalSize)} / ${formatSize(space.maxSize)}`"
        >
          <a-progress
            type="circle"
            :size="42"
            :percent="((space.totalSize * 100) / space.maxSize).toFixed(1)"
          />
        </a-tooltip>

      </a-space>
    </a-flex>

    <div style="margin-bottom: 16px" />
    <!-- 搜索表单 -->
    <PictureSearchForm :onSearch="onSearch" />
    <div style="margin-bottom: 16px" />

    <!-- 按颜色搜索 -->
    <a-form-item label="按颜色搜索" style="margin-top: 16px">
      <color-picker format="hex" @pureColorChange="onColorChange" />
    </a-form-item>
    <div style="margin-bottom: 16px" />
    <!-- 图片列表 -->
    <PictureList
      :dataList="dataList"
      :loading="loading"
      :showOp="true"
      :canEdit="canEditPicture"
      :canDelete="canDeletePicture"
      :onReload="fetchData"
    />


    <!-- 分页 -->
    <a-pagination
      style="text-align: right"
      v-model:current="searchParams.current"
      v-model:pageSize="searchParams.pageSize"
      :total="total"
      @change="onPageChange"
    />

    <!-- 批量编辑组件   -->
    <BatchEditPictureModel
      ref="batchEditPictureModalRef"
      :spaceId="id"
      :pictureList="dataList"
      :onSuccess="onBatchEditPictureSuccess"
    />
  </div>
</template>
<script setup lang="ts">
import {onMounted, reactive, ref, h, computed, watch} from 'vue'
import { getSpaceVoByIdUsingGet } from '@/api/spaceController.ts'
import { message } from 'ant-design-vue'
import {listPictureVoByPageUsingPost, searchPictureByColorUsingPost} from '@/api/pictureController.ts'
import { formatSize } from '@/utils'
import PictureList from '@/components/PictureList.vue'
import PictureSearchForm from '@/components/PictureSearchForm.vue'
import {ColorPicker} from "vue3-colorpicker";
import 'vue3-colorpicker/style.css'
import BatchEditPictureModel from "@/components/BatchEditPictureModel.vue";
import { EditOutlined,BarChartOutlined,TeamOutlined } from '@ant-design/icons-vue'
import {SPACE_PERMISSION_ENUM, SPACE_TYPE_MAP} from "@/constant/space"
import _default from "ant-design-vue/lib/vc-slick/inner-slider";
/**
 * 向子页面传值
 */
const props = defineProps<Props>()
interface Props {
  id: string | number
}

// 数据
const space = ref<API.SpaceVO>({})

onMounted(() => {
  fetchSpaceDetail()
})

// 通用权限检查函数
function createPermissionChecker(permission: string) {
  return computed(() => {
    return (space.value.permissionList ?? []).includes(permission)
  })
}

function hiddenUserManage(permission: string) {
  console.log(space.value.spaceType)
  return computed(() => {
    console.log(space.value.spaceType)
    return (space.value.permissionList ?? []).includes(permission) && space.value.spaceType === 1
  })
}
// 定义权限检查
const canManageSpaceUser = createPermissionChecker(SPACE_PERMISSION_ENUM.SPACE_USER_MANAGE)
const canUploadPicture = createPermissionChecker(SPACE_PERMISSION_ENUM.PICTURE_UPLOAD)
const canEditPicture = createPermissionChecker(SPACE_PERMISSION_ENUM.PICTURE_EDIT)
const canDeletePicture = createPermissionChecker(SPACE_PERMISSION_ENUM.PICTURE_DELETE)

const needHiddenUserManage = hiddenUserManage(SPACE_PERMISSION_ENUM.SPACE_USER_MANAGE)

// 空间 id 改变时，必须重新获取数据
watch(
  () => props.id,
  (newSpaceId) => {
    fetchSpaceDetail()
    fetchData()
  },
)

// 1. -------- 获取空间详情 --------
const fetchSpaceDetail = async () => {
  try {
    const res = await getSpaceVoByIdUsingGet({
      id: props.id,
    } as API.getSpaceByIdUsingGETParams)
    if (res.data.code === 0 && res.data.data) {
      space.value = res.data.data
    } else {
      message.error('获取空间详情失败，' + res.data.message)
    }
  } catch (e: any) {
    message.error('获取空间详情失败：' + e.message)
  }
}

// 2. --------- 获取图片列表 --------

// 定义数据
const dataList = ref<API.PictureVO[]>([])
const total = ref(0)
const loading = ref(true)

// 搜索条件
const searchParams = ref<API.PictureQueryRequest>({
  current: 1,
  pageSize: 12,
  sortFiled: 'createTime',
  sortOrder: 'descend',
})

// 页面加载时获取数据，请求一次
onMounted(() => {
  fetchData()
})

// 分页参数
const onPageChange = (page: number, pageSize: number) => {
  searchParams.value.current = page
  searchParams.value.pageSize = pageSize
  fetchData()
}

// 获取数据
const fetchData = async () => {
  loading.value = true
  // 转换搜索参数
  const params = {
    spaceId: props.id,
    ...searchParams.value,
  }
  const res = await listPictureVoByPageUsingPost(params)
  if (res.data.code === 0 && res.data.data) {
    dataList.value = res.data.data.records ?? []
    total.value = res.data.data.total ?? 0
  } else if (res.data.code === 0 && res.data.data === null){
    dataList.value =  []
    total.value = 0
  }else {
    message.error('获取数据失败，' + res.data.message)
  }
  loading.value = false
}

// 3. 查询函数
const onSearch = (newSearchParams: API.PictureQueryRequest) => {
  searchParams.value = {
    ...searchParams.value,
    ...newSearchParams,
    current: 1,
  }
  fetchData()
}

// 4. 以色搜图
const onColorChange = async (color: string) => {
  const res = await searchPictureByColorUsingPost({
    picColor: color,
    spaceId: props.id,
  } as API.SearchPictureByColorRequest)
  if (res.data.code === 0 && res.data.data) {
    const data = res.data.data ?? [];
    dataList.value = data;
    total.value = data.length;
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
}

// ---- 批量编辑图片 -----
const batchEditPictureModalRef = ref()
// 批量编辑图片成功
const onBatchEditPictureSuccess = () => {
  fetchData()
}
// 打开批量编辑图片弹窗
const doBatchEdit = () => {
  if (batchEditPictureModalRef.value) {
    batchEditPictureModalRef.value.openModal()
  }
}

</script>
<style scoped>
#spaceDetailPage {
  margin-bottom: 16px;
}
</style>
