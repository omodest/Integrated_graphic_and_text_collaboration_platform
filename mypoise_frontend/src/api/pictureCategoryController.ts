// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** addCategory POST /api/picture/category/add */
export async function addCategoryUsingPost(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.addCategoryUsingPOSTParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean_>('/api/picture/category/add', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** delCategory POST /api/picture/category/del */
export async function delCategoryUsingPost(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.delCategoryUsingPOSTParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean_>('/api/picture/category/del', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** editCategory POST /api/picture/category/edit */
export async function editCategoryUsingPost(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.editCategoryUsingPOSTParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean_>('/api/picture/category/edit', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** queryCategory POST /api/picture/category/get */
export async function queryCategoryUsingPost(options?: { [key: string]: any }) {
  return request<API.BaseResponseListPictureCategory_>('/api/picture/category/get', {
    method: 'POST',
    ...(options || {}),
  })
}
