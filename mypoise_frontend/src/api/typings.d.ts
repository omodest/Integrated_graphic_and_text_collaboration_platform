declare namespace API {
  type addCategoryUsingPOSTParams = {
    /** categoryName */
    categoryName?: string
  }

  type BaseResponseBoolean_ = {
    code?: number
    data?: boolean
    message?: string
  }

  type BaseResponseInt_ = {
    code?: number
    data?: number
    message?: string
  }

  type BaseResponseListPictureCategory_ = {
    code?: number
    data?: PictureCategory[]
    message?: string
  }

  type BaseResponseListPictureTags_ = {
    code?: number
    data?: PictureTags[]
    message?: string
  }

  type BaseResponseLong_ = {
    code?: number
    data?: number
    message?: string
  }

  type BaseResponsePagePicture_ = {
    code?: number
    data?: PagePicture_
    message?: string
  }

  type BaseResponsePagePictureVO_ = {
    code?: number
    data?: PagePictureVO_
    message?: string
  }

  type BaseResponsePageUserInfoVO_ = {
    code?: number
    data?: PageUserInfoVO_
    message?: string
  }

  type BaseResponsePicture_ = {
    code?: number
    data?: Picture
    message?: string
  }

  type BaseResponsePictureVO_ = {
    code?: number
    data?: PictureVO
    message?: string
  }

  type BaseResponseString_ = {
    code?: number
    data?: string
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

  type delCategoryUsingPOSTParams = {
    /** categoryId */
    categoryId?: number
  }

  type DeleteRequest = {
    id?: number
  }

  type deleteUserUsingPOSTParams = {
    /** id */
    id?: number
  }

  type delTagsUsingPOSTParams = {
    /** tagId */
    tagId?: number
  }

  type editCategoryUsingPOSTParams = {
    /** categoryId */
    categoryId?: number
    /** categoryName */
    categoryName?: string
  }

  type getCaptchaUsingGETParams = {
    /** email */
    email?: string
    /** repeat */
    repeat?: boolean
  }

  type getPictureByIdUsingGETParams = {
    /** id */
    id?: number
  }

  type getPictureVOByIdUsingGETParams = {
    /** id */
    id?: number
  }

  type getUserByIdUsingPOSTParams = {
    /** id */
    id?: number
  }

  type getUserVoByIdUsingPOSTParams = {
    /** id */
    id?: number
  }

  type PagePicture_ = {
    current?: number
    pages?: number
    records?: Picture[]
    size?: number
    total?: number
  }

  type PagePictureVO_ = {
    current?: number
    pages?: number
    records?: PictureVO[]
    size?: number
    total?: number
  }

  type PageUserInfoVO_ = {
    current?: number
    pages?: number
    records?: UserInfoVO[]
    size?: number
    total?: number
  }

  type Picture = {
    categoryId?: number
    createTime?: string
    editTime?: string
    id?: number
    introduction?: string
    isDelete?: number
    name?: string
    picFormat?: string
    picHeight?: number
    picScale?: number
    picSize?: number
    picWidth?: number
    tagNames?: string[]
    updateTime?: string
    url?: string
    userId?: number
  }

  type PictureCategory = {
    applyTotal?: number
    categoryName?: string
    createTime?: string
    id?: number
    isDelete?: number
    updateTime?: string
  }

  type PictureEditRequest = {
    category?: number
    id?: number
    introduction?: string
    name?: string
    tags?: string[]
  }

  type PictureQueryRequest = {
    category?: string
    current?: number
    id?: number
    introduction?: string
    name?: string
    pageSize?: number
    picFormat?: string
    picHeight?: number
    picScale?: number
    picSize?: number
    picWidth?: number
    searchText?: string
    sortFiled?: string
    sortOrder?: string
    tags?: string[]
    userId?: number
  }

  type PictureTags = {
    applyTotal?: number
    createTime?: string
    id?: number
    isDelete?: number
    tagName?: string
    updateTime?: string
    userId?: number
  }

  type PictureUpdateRequest = {
    category?: number
    id?: number
    introduction?: string
    name?: string
    tags?: string[]
  }

  type PictureVO = {
    categoryId?: string
    createTime?: string
    editTime?: string
    id?: number
    introduction?: string
    name?: string
    picFormat?: string
    picHeight?: number
    picScale?: number
    picSize?: number
    picWidth?: number
    tagNames?: string[]
    updateTime?: string
    url?: string
    user?: UserInfoVO
    userId?: number
  }

  type testDownloadFileUsingGETParams = {
    /** filepath */
    filepath?: string
  }

  type uploadFileUsingPOSTParams = {
    biz?: string
  }

  type uploadPictureUsingPOSTParams = {
    id?: number
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
    userName?: string
    userProfile?: string
    userRole?: string
    vip_expire?: string
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

  type UserUpdateRequest = {
    id?: number
    userAccount?: string
    userName?: string
    userProfile?: string
    userRole?: string
    vip_expire?: string
  }
}
