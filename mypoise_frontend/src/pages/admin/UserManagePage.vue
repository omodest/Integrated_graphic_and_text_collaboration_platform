<template>
  <div id="userManagePage">
    <!-- 搜索表单 -->
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
      <a-form-item label="账号">
        <a-input v-model:value="searchParams.userAccount" placeholder="输入账号" allow-clear />
      </a-form-item>
      <a-form-item label="用户名">
        <a-input v-model:value="searchParams.userName" placeholder="输入用户名" allow-clear />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit">搜索</a-button>
      </a-form-item>
      <a-form-item>
        <a-button type="primary" class="editable-add-btn" style="margin-bottom: 8px" @click="handleAdd">Add</a-button>
      </a-form-item>
    </a-form>
    <div style="margin-bottom: 16px" />
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
        <template v-if="column.dataIndex === 'userAvatar'">
          <a-image :src="record.userAvatar" :width="25" />
        </template>
        <template v-else-if="column.dataIndex === 'userRole'">
          <div v-if="record.userRole === 'admin'">
            <a-tag color="green">管理员</a-tag>
          </div>
          <div v-else-if="record.userRole === 'vip'">
            <a-tag color="red">VIP</a-tag>
          </div>
          <div v-else-if="record.userRole === 'admin'">
            <a-tag color="yellow">FVIP</a-tag>
          </div>
          <div v-else>
            <a-tag color="blue">普通用户</a-tag>
          </div>
        </template>
        <!-- 设置可编辑的单元格      -->
        <template v-if="column.dataIndex === 'createTime'">
          {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.dataIndex === 'userAccount'">
          <a-input  style="width: 100px;"  v-model:value="record.userAccount" @blur="onSave(record, 'userAccount')" />
        </template>
        <template v-else-if="column.dataIndex === 'userName'">
          <a-input style="width: 100px;" v-model:value="record.userName" @blur="onSave(record, 'userName')" />
        </template>
        <template v-else-if="column.dataIndex === 'userProfile'">
          <a-input style="width: 200px;" v-model:value="record.userProfile" @blur="onSave(record, 'userProfile')" />
        </template>
        <template v-else-if="column.dataIndex === 'userRole'">
          <a-select v-model:value="record.userRole" @change="onSave(record, 'userRole')" placeholder="选择角色">
            <a-select-option value="admin">管理员</a-select-option>
            <a-select-option value="user">普通用户</a-select-option>
            <a-select-option value="fvip">fvip</a-select-option>
          </a-select>
        </template>
        <template v-else-if="column.dataIndex === 'vip_expire' && record.vip_expire != null">
          {{record.vip_expire}}
        </template>
        <template v-else-if="column.dataIndex === 'vip_expire' && record.vip_expire == null">
          无
        </template>
        <template v-else-if="column.key === 'action'">
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

// 定义数据列表
const dataList = ref<API.UserInfoVO[]>([])
// 总条数
const total = ref(0)
// 搜索条件
const searchParams = reactive<API.UserQueryRequest>({
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
  const res = await getListUserVoByPageUsingPost({
    ...searchParams,
  })
  if (res.data.code === 0 && res.data.data) {
    dataList.value = res.data.data.records ?? []
    total.value = res.data.data.total ?? 0
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
}

// 2. 删除数据
const doDelete = async (id: string) => {
  if (!id) {
    return
  }
  const res = await deleteUserUsingPost({ id })
  if (res.data.code === 0) {
    message.success('删除成功')
    // 刷新数据
    await fetchData()
  } else {
    message.error('删除失败')
  }
}

// 3. 管理员 编辑保存函数
const onSave = async (record: API.UserInfoVO, field: string) => {
  // 触发更新接口请求
  const updatedData = {
    id: record.id,
    [field]: record[field],
  }
  const response = await updateUserUsingPost(updatedData)
  if (response.data.code === 0){
    message.success('更新成功')
  }

}

// 4. 管理员添加用户
// 获取当前时间戳（毫秒级）
const timestamp = Date.now();
// 生成一个随机的 4 位十六进制数
const random = Math.floor(Math.random() * 0x10000).toString(16).padStart(4, '0');
const handleAdd = async () => {
  // 新建的空数据对象，初始化为空
  const newData = {
    userAccount: `${timestamp}-${random}`,
    userName: `${timestamp}-${random}`,
    userProfile: "无",
    userRole: "user",
  };

  // 将新数据添加到列表中，显示新增行
  dataList.value.unshift(newData); // 使用 unshift 使新数据出现在最前面

  try {
    // 提交新增用户数据请求
    const response = await createUserUsingPost(newData);
    if (response.data.code === 0) {
      message.success('用户添加成功,请及时修改用户账号等信息。');
      await fetchData();
    } else {
      message.error('添加用户失败，' + response.data.message);
      dataList.value.shift();
    }
  } catch (error) {
    message.error('添加用户请求失败，请稍后再试');
    dataList.value.shift();
  }
};

/**
 * 定义数据列
 */
const columns = [
  {
    title: 'id',
    dataIndex: 'id',
  },
  {
    title: '账号',
    dataIndex: 'userAccount',
  },
  {
    title: '用户名',
    dataIndex: 'userName',
  },
  {
    title: '头像',
    dataIndex: 'userAvatar',
  },
  {
    title: '简介',
    dataIndex: 'userProfile',
  },
  {
    title: '用户角色',
    dataIndex: 'userRole',
  },
  {
    title: '会员过期时间',
    dataIndex: 'vip_expire',
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
