declare namespace API {
  type BaseResponseBoolean_ = {
    code?: number
    data?: boolean
    message?: string
  }

  type BaseResponseLong_ = {
    code?: number
    data?: number
    message?: string
  }

  type BaseResponsePageUserInfoVO_ = {
    code?: number
    data?: PageUserInfoVO_
    message?: string
  }

  type BaseResponseUser_ = {
    code?: number
    data?: User
    message?: string
  }

  type BaseResponseUserInfoVO_ = {
    code?: number
    data?: UserInfoVO
    message?: string
  }

  type deleteUserUsingPOSTParams = {
    /** id */
    id?: number
  }

  type getCaptchaUsingGETParams = {
    /** email */
    email?: string
  }

  type getUserByIdUsingPOSTParams = {
    /** id */
    id?: number
  }

  type getUserVoByIdUsingPOSTParams = {
    /** id */
    id?: number
  }

  type PageUserInfoVO_ = {
    current?: number
    pages?: number
    records?: UserInfoVO[]
    size?: number
    total?: number
  }

  type User = {
    createTime?: string
    editTime?: string
    email?: string
    gender?: string
    id?: number
    isDelete?: number
    updateTime?: string
    userAccount?: string
    userAvatar?: string
    userName?: string
    userPassword?: string
    userProfile?: string
    userRole?: string
    vip_expire?: string
    wechat_id?: number
  }

  type UserAddRequest = {
    userAccount?: string
    userAvatar?: string
    userName?: string
    userProfile?: string
    userRole?: string
  }

  type UserBindRequest = {
    captcha?: string
    email?: string
  }

  type UserEmailEditPwdRequest = {
    captcha?: string
    email?: string
    userPassword?: string
  }

  type UserEmailLoginRequest = {
    captcha?: string
    email?: string
  }

  type UserInfoVO = {
    createTime?: string
    editTime?: string
    email?: string
    gender?: string
    id?: number
    updateTime?: string
    userAccount?: string
    userAvatar?: string
    userName?: string
    userProfile?: string
    userRole?: string
    vip_expire?: string
    wechat_id?: number
  }

  type UserLoginRequest = {
    userAccount?: string
    userPassword?: string
  }

  type UserQueryRequest = {
    current?: number
    id?: number
    pageSize?: number
    sortFiled?: string
    sortOrder?: string
    userAccount?: string
    userName?: string
    userProfile?: string
    userRole?: string
  }

  type UserRegisterRequest = {
    captcha?: string
    email?: string
    userAccount?: string
    userPassword?: string
  }
}
