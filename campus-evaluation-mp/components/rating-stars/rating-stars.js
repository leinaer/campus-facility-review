// components/rating-stars/rating-stars.js
Component({
  properties: {
    value: {
      type: Number,
      value: 5,
      observer(newVal) {
        this.setData({ value: newVal });
      }
    },
    showText: {
      type: Boolean,
      value: false
    }
  },

  data: {
    value: 5
  },

  methods: {
    onStarTap(e) {
      const value = e.currentTarget.dataset.value;
      this.setData({ value });
      this.triggerEvent('change', { value });
    }
  }
});
