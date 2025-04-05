Component({
  properties: {
    imageList: {
      type: Array,
      value: []
    },
    columnCount: {
      type: Number,
      value: 2
    }
  },

  data: {
    columns: []
  },

  observers: {
    'imageList': function(newVal) {
      this.distributeImages();
    }
  },

  methods: {
    // 初始化列数据
    initColumns() {
      const columns = Array.from({ length: this.properties.columnCount }, () => []);
      this.setData({ columns });
    },

    // 找出高度最小的列
    findMinHeightColumn() {
      const heights = this.data.columns.map(column => {
        return column.reduce((total, item) => total + (item.height || 0), 0);
      });
      return heights.indexOf(Math.min(...heights));
    },

    // 分配图片到各列
    distributeImages() {
      this.initColumns();
      const columns = this.data.columns;
      
      this.properties.imageList.forEach(image => {
        const minHeightColumnIndex = this.findMinHeightColumn();
        columns[minHeightColumnIndex].push(image);
      });

      this.setData({ columns });
    },

    // 处理图片点击
    handleImageClick(e) {
      const { columnIndex, imageIndex } = e.currentTarget.dataset;
      const item = this.data.columns[columnIndex][imageIndex];
      this.triggerEvent('imageClick', item);
    },
  }
}); 