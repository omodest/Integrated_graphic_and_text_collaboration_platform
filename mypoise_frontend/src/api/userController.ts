// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** getCaptcha GET /api/user/captcha */
export async function getCaptchaUsingGet(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getCaptchaUsingGETParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean_>('/api/user/captcha', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** createUser POST /api/user/create */
export async function createUserUsingPost(
  body: API.UserAddRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseLong_>('/api/user/create', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** deleteUser POST /api/user/delete */
export async function deleteUserUsingPost(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.deleteUserUsingPOSTParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean_>('/api/user/delete', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** doSign POST /api/user/doSign */
export async function doSignUsingPost(options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean_>('/api/user/doSign', {
    method: 'POST',
    ...(options || {}),
  })
}

/** userEditMyVo POST /api/user/edit/my/vo */
export async function userEditMyVoUsingPost(
  body: API.UserInfoVO,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseUserInfoVO_>('/api/user/edit/my/vo', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** userEmailBind POST /api/user/email/bind */
export async function userEmailBindUsingPost(
  body: API.UserBindRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean_>('/api/user/email/bind', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** userEmailEditPassword POST /api/user/email/edit/password */
export async function userEmailEditPasswordUsingPost(
  body: API.UserEmailEditPwdRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean_>('/api/user/email/edit/password', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** getUserById POST /api/user/get/by/id */
export async function getUserByIdUsingPost(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getUserByIdUsingPOSTParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseUser_>('/api/user/get/by/id', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** getListUserVoByPage POST /api/user/get/list/user/vo */
export async function getListUserVoByPageUsingPost(
  body: API.UserQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageUserInfoVO_>('/api/user/get/list/user/vo', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** getLoginUser GET /api/user/get/login */
export async function getLoginUserUsingGet(options?: { [key: string]: any }) {
  return request<API.BaseResponseUserInfoVO_>('/api/user/get/login', {
    method: 'GET',
    ...(options || {}),
  })
}

/** getSignNum GET /api/user/get/totalSign */
export async function getSignNumUsingGet(options?: { [key: string]: any }) {
  return request<API.BaseResponseInt_>('/api/user/get/totalSign', {
    method: 'GET',
    ...(options || {}),
  })
}

/** getUserVoById POST /api/user/get/vo/by/id */
export async function getUserVoByIdUsingPost(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getUserVoByIdUsingPOSTParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseUserInfoVO_>('/api/user/get/vo/by/id', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** userLogin POST /api/user/login */
export async function userLoginUsingPost(
  body: API.UserLoginRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseUserInfoVO_>('/api/user/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** userLoginByEmail POST /api/user/login/email */
export async function userLoginByEmailUsingPost(
  body: API.UserEmailLoginRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseUserInfoVO_>('/api/user/login/email', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** userLogout POST /api/user/logout */
export async function userLogoutUsingPost(options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean_>('/api/user/logout', {
    method: 'POST',
    ...(options || {}),
  })
}

/** doEmailRegister POST /api/user/register */
export async function doEmailRegisterUsingPost(
  body: API.UserRegisterRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseLong_>('/api/user/register', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** updateUser POST /api/user/update */
export async function updateUserUsingPost(
  body: API.UserUpdateRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseUserInfoVO_>('/api/user/update', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
