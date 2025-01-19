// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** addCategory POST /api/picture/category/add */
export async function addCategoryUsingPost(body: string, options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean_>('/api/picture/category/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** delCategory POST /api/picture/category/del */
export async function delCategoryUsingPost(body: number, options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean_>('/api/picture/category/del', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** editCategory POST /api/picture/category/edit */
export async function editCategoryUsingPost(
  body: API.CategoryEditRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean_>('/api/picture/category/edit', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
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
