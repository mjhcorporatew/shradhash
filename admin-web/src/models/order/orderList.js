import { message } from 'antd';
import { orderPage, updateOrderItem, updateOrderItemPayAmount } from '../../services/order';

export default {
  namespace: 'orderList',

  state: {
    list: {
      pagination: {
        current: 0,
        pageSize: 10,
        total: 0,
      },
      dataSource: [],
    },
    payAmountVisible: false,
    payAmount: 0,
    orderId: 0,
    orderItemId: 0,
    searchParams: {},
  },

  effects: {
    *queryPage({ payload }, { call, put }) {
      const response = yield call(orderPage, payload);

      yield put({
        type: 'changeSearchParams',
        payload: {
          searchParams: {
            ...payload,
          },
        },
      });

      message.info('查询成功!', response);
      const { total, orders } = response.data;
      yield put({
        type: 'queryPageSuccess',
        payload: {
          list: {
            dataSource: orders,
            pagination: {
              total,
              current: payload.pageNo,
              pageSize: payload.pageSize,
            },
          },
        },
      });
    },
    *updateOrderItem({ payload }, { call, put }) {
      const { params } = payload;
      const response = yield call(updateOrderItem, params);
      message.info('查询成功!');
      yield put({
        type: 'queryPageSuccess',
        payload: {
          list: response.data,
        },
      });
    },
    *updatePayAmount({ payload }, { call, put }) {
      const { searchParams, params } = payload;
      yield call(updateOrderItemPayAmount, params);
      yield put({
        type: 'changePayAmountVisible',
        payload: {
          payAmountVisible: false,
        },
      });

      yield put({
        type: 'queryPage',
        payload: {
          ...searchParams,
        },
      });
    },
  },

  reducers: {
    queryPageSuccess(state, { payload }) {
      const { list } = payload;
      return {
        ...state,
        list,
      };
    },
    changePayAmountVisible(state, { payload }) {
      return {
        ...state,
        ...payload,
      };
    },
    changeSearchParams(state, { payload }) {
      return {
        ...state,
        ...payload,
      };
    },
  },
};
