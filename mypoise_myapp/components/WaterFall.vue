<template>
    <!-- 图片瀑布流展示 -->
  <view class="waterfall-container">
    <view class="waterfall-column" v-for="(column, columnIndex) in columns" :key="columnIndex">
      <view
        class="waterfall-item"
        v-for="(item, index) in column"
        :key="index"
        @click="handleImageClick(item)"
      >
        <image
          :src="item.imageUrl"
          :style="{ height: item.height + 'px' }"
          mode="widthFix"
          @load="imageLoad(columnIndex, index, $event)"
        />
        <view class="image-info" v-if="item.title">
          <text class="title">{{ item.title }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';

// 定义组件属性
const props = defineProps({
  // 图片列表数据
  imageList: {
    type: Array,
    default: () => []
  },
  // 列数
  columnCount: {
    type: Number,
    default: 2
  }
});

// 定义事件
const emit = defineEmits(['imageClick']);

// 列数据
const columns = ref<any[]>([]);

// 初始化列数据
const initColumns = () => {
  columns.value = Array.from({ length: props.columnCount }, () => []);
};

// 处理图片加载
const imageLoad = (columnIndex: number, index: number, event: any) => {
  const { width, height } = event.detail;
  if (columns.value[columnIndex][index]) {
    columns.value[columnIndex][index].height = height;
  }
};

// 找出高度最小的列
const findMinHeightColumn = () => {
  const heights = columns.value.map(column => {
    return column.reduce((total, item) => total + (item.height || 0), 0);
  });
  return heights.indexOf(Math.min(...heights));
};

// 分配图片到各列
const distributeImages = () => {
  initColumns();
  props.imageList.forEach((image: any) => {
    const minHeightColumnIndex = findMinHeightColumn();
    columns.value[minHeightColumnIndex].push({
      ...image,
      height: 0 // 初始高度为0，等待图片加载后更新
    });
  });
};

// 监听图片列表变化
watch(() => props.imageList, () => {
  distributeImages();
}, { deep: true });

// 处理图片点击
const handleImageClick = (item: any) => {
  emit('imageClick', item);
};

// 组件挂载时初始化
onMounted(() => {
  distributeImages();
});
</script>

<style scoped>
.waterfall-container {
  display: flex;
  padding: 10rpx;
  box-sizing: border-box;
}

.waterfall-column {
  flex: 1;
  padding: 0 10rpx;
}

.waterfall-item {
  margin-bottom: 20rpx;
  border-radius: 8rpx;
  overflow: hidden;
  background-color: #fff;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.1);
}

.waterfall-item image {
  width: 100%;
  display: block;
}

.image-info {
  padding: 16rpx;
}

.title {
  font-size: 28rpx;
  color: #333;
  line-height: 1.4;
}
</style> 