// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** delTags POST /api/picture/tags/del */
export async function delTagsUsingPost(body: number, options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean_>('/api/picture/tags/del', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** queryTags POST /api/picture/tags/get */
export async function queryTagsUsingPost(options?: { [key: string]: any }) {
  return request<API.BaseResponseListPictureTags_>('/api/picture/tags/get', {
    method: 'POST',
    ...(options || {}),
  })
}

/** getHotTags GET /api/picture/tags/hot */
export async function getHotTagsUsingGet(options?: { [key: string]: any }) {
  return request<API.BaseResponseListPictureTags_>('/api/picture/tags/hot', {
    method: 'GET',
    ...(options || {}),
  })
}
