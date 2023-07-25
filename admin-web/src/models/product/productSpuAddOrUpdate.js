import { message } from 'antd';
import {
  productCategoryTree,
  productSpuAdd,
  productCategoryUpdate,
  productCategoryUpdateStatus,
  productCategoryDelete,
  productSpuInfo
} from '../../services/product';

export default {
  namespace: 'productSpuAddOrUpdate',

  state: {
    // list: [],
    loading: false,
    spu: { // 商品 SPU

    },

    attrTree: [ // 商品规格
      // {
      //   id: //
      //   name: //
      //   values: [{
      //      id: //
      //      name: //
      //   }]
      // }
    ],
    skus: [ // 商品 SKU
      // {
      //   attrs: [{
      //     id: // 规格值编号
      //     name: // 规格值名
      //   }],
      //   price: // 价格
      //   quantity: // 数量
      // }
    ]
  },

  effects: {
    // *update({ payload }, { call, put }) {
    //   const { callback, body } = payload;
    //   const response = yield call(productCategoryUpdate, body);
    //   if (callback) {
    //     callback(response);
    //   }
    //   yield put({
    //     type: 'tree',
    //     payload: {},
    //   });
    // },
    *info({ payload }, { call, put }) {
      // 显示加载中
      yield put({
        type: 'changeLoading',
        payload: true,
      });

      // 请求
      const response = yield call(productSpuInfo, {
        id: payload,
      });
      // 响应
      let skus = [];
      let attrTree = [];
      // SKU
      for (let i in response.data.skus) {
        let sku = response.data.skus[i];
        // 处理 sku
        {
          let attrs = [];
          for (let j in sku.attrs) {
            let attr = sku.attrs[j];
            attrs.push({
              id: attr.attrValueId,
              name: attr.attrValueName,
            });
          }
          let newSku = {
            ...sku,
            attrs,
          };
          skus.push(newSku);
        }
        // 处理 attrTree
        {
          for (let j in sku.attrs) {
            // debugger;
            let attr = sku.attrs[j];
            let attrTreeNode = undefined;
            for (let k in attrTree) {
              let item = attrTree[k];
              if (item.id === attr.attrId) {
                attrTreeNode = item;
                break;
              }
            }
            if (!attrTreeNode) {
              attrTreeNode = {
                id: attr.attrId,
                name: attr.attrName,
                values: [{
                  id: attr.attrValueId,
                  name: attr.attrValueName,
                }]
              };
              attrTree.push(attrTreeNode);
            } else {
              // let values = attrTreeNode.values;
              // for (let k in ) {
              //
              // }
            }
          }
        }
      }
      // debugger;
      yield put({
        type: 'setAll',
        payload: {
          spu: response.data,
          skus: skus,
          attrTree: attrTree,
        },
      });

      // 隐藏加载中
      yield put({
        type: 'changeLoading',
        payload: false,
      });
    },
    *addAttr({ payload }, { call, put }) {
      // const { queryParams } = payload;
      // const response = yield call(productCategoryTree, queryParams);
      // message.info('调试：添加规格成功!');
      yield put({
        type: 'addAttrSuccess',
        payload: {
          attrAdd: {},
        },
      });
    },
    *selectAttr({ payload }, { call, put }) {
      // const { queryParams } = payload;
      // const response = yield call(productCategoryTree, queryParams);
      // message.info('调试：选择规格成功!');
      yield put({
        type: 'selectAttrSuccess',
        payload: payload,
      });
    },
    *selectAttrValues({ payload }, { call, put }) {
      // const { queryParams } = payload;
      // const response = yield call(productCategoryTree, queryParams);
      // message.info('调试：选择规格值成功!');
      yield put({
        type: 'selectAttrValueSuccess',
        payload: payload,
      });
    },
    *inputSkuPrice({ payload }, { call, put }) {
      // debugger;
      yield put({
        type: 'inputSkuPriceSuccess',
        payload: payload,
      });
    },
    *inputSkuQuantity({ payload }, { call, put }) {
      // debugger;
      yield put({
        type: 'inputSkuQuantitySuccess',
        payload: payload,
      });
    },
    *add({ payload }, { call, put }) {
      const { callback, body } = payload;
      const response = yield call(productSpuAdd, body);
      if (callback) {
        callback(response);
      }
      // yield put({
      //   type: 'tree',
      //   payload: {},
      // });
      alert('添加成功！后续改成跳转到手机站的详情');
    },
    *update({ payload }, { call, put }) {
      const { callback, body } = payload;
      const response = yield call(productSpuAdd, body);
      if (callback) {
        callback(response);
      }
      // yield put({
      //   type: 'tree',
      //   payload: {},
      // });
      alert('修改成功！后续改成跳转到手机站的详情');
    },
  },

  reducers: {
    addAttrSuccess(state, {payload}) {
      // debugger;
      // console.log(state.attrTree);
      state.attrTree.push(payload.attrAdd);
      return {
        ...state
      }
    },
    selectAttrSuccess(state, {payload}) {
      // debugger;
      // console.log(state.attrTree);
      state.attrTree[payload.attrIndex] = payload.attr;
      return {
        ...state
      }
    },
    selectAttrValueSuccess(state, {payload}) {
      // debugger;
      // console.log(state);
      state.attrTree[payload.attrIndex].values = payload.attrValues;
      // 生成 skus 值
      let skus = [];
      let skuSize = 1;
      for (let i in state.attrTree) { // 先计算 sku 数量
        let attr = state.attrTree[i];
        skuSize = skuSize * attr.values.length;
      }
      // console.log('skuSize: ' + skuSize);
      for (let i = 0; i < skuSize; i++) { // 初始化 sku 格子
        skus.push({
          attrs: [],
          price: undefined,
          quantity: undefined,
        });
      }
      for (let i = 0; i < state.attrTree.length; i++) { // 初始化 sku 格子里的 attrs
        for (let j = 0; j < skuSize; j++) {
          // let values = state.attrTree[i].values;
          // let attr = values[j % values.length];
          // skus[i].attrs.push({
          //   id: attr.id,
          //   name: attr.name,
          // });
          let values = state.attrTree[i].values;
          let attr = values[j % values.length];
          skus[j].attrs.push({
            id: attr.id,
            name: attr.name,
          });
        }
      }
      state.skus = skus;
      // debugger;
      // console.l  og('skus: ' + skus);
      return {
        ...state
      }
    },
    inputSkuPriceSuccess(state, {payload}) {
      // debugger;
      state.skus[payload.index].price = payload.price;
      return {
        ...state
      }
    },
    inputSkuQuantitySuccess(state, {payload}) {
      // debugger;
      state.skus[payload.index].quantity = payload.quantity;
      return {
        ...state
      }
    },
    clear(state, {payload}) {
      return {
        ...state,
        skus: [],
        attrTree: [],
      }
    },
    changeLoading(state, { payload }) {
      return {
        ...state,
        listLoading: payload,
      };
    },
    // 设置所有属性
    setAll(state, { payload }) {
      return {
        ...state,
        ...payload,
      };
    }
    // treeSuccess(state, { payload }) {
    //   return {
    //     ...state,
    //     ...payload,
    //   };
    // },
  },
};
