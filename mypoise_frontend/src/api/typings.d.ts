declare namespace API {
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

  type BaseResponseListImageSearchResult_ = {
    code?: number
    data?: ImageSearchResult[]
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

  type BaseResponseListPictureVO_ = {
    code?: number
    data?: PictureVO[]
    message?: string
  }

  type BaseResponseListSpaceLevel_ = {
    code?: number
    data?: SpaceLevel[]
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

  type BaseResponsePageSpace_ = {
    code?: number
    data?: PageSpace_
    message?: string
  }

  type BaseResponsePageSpaceVO_ = {
    code?: number
    data?: PageSpaceVO_
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

  type BaseResponseSpace_ = {
    code?: number
    data?: Space
    message?: string
  }

  type BaseResponseSpaceVO_ = {
    code?: number
    data?: SpaceVO
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

  type CategoryEditRequest = {
    categoryId?: number
    categoryName?: string
  }

  type DeleteRequest = {
    id?: number
  }

  type deleteUserUsingPOSTParams = {
    /** id */
    id?: number
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

  type getSpaceByIdUsingGETParams = {
    /** id */
    id?: number
  }

  type getSpaceVOByIdUsingGETParams = {
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

  type ImageSearchResult = {
    fromUrl?: string
    thumbUrl?: string
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

  type PageSpace_ = {
    current?: number
    pages?: number
    records?: Space[]
    size?: number
    total?: number
  }

  type PageSpaceVO_ = {
    current?: number
    pages?: number
    records?: SpaceVO[]
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
    category?: string
    categoryId?: number
    createTime?: string
    editTime?: string
    endEditTime?: string
    id?: number
    introduction?: string
    isDelete?: number
    name?: string
    picColor?: string
    picFormat?: string
    picHeight?: number
    picScale?: number
    picSize?: number
    picWidth?: number
    reviewMessage?: string
    reviewStatus?: number
    reviewTime?: string
    reviewerId?: number
    spaceId?: number
    startEditTime?: string
    tagNames?: string[]
    thumbnailUrl?: string
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

  type PictureEditByBatchRequest = {
    category?: string
    nameRule?: string
    pictureIdList?: number[]
    spaceId?: number
    tags?: string[]
  }

  type PictureEditRequest = {
    category?: string
    id?: number
    introduction?: string
    name?: string
    tags?: string[]
  }

  type PictureQueryRequest = {
    category?: string
    current?: number
    endEditTime?: string
    id?: number
    introduction?: string
    name?: string
    nullSpaceId?: boolean
    pageSize?: number
    picFormat?: string
    picHeight?: number
    picScale?: number
    picSize?: number
    picWidth?: number
    reviewMessage?: string
    reviewStatus?: number
    reviewerId?: number
    searchText?: string
    sortFiled?: string
    sortOrder?: string
    spaceId?: number
    startEditTime?: string
    tags?: string[]
    userId?: number
  }

  type PictureReviewRequest = {
    id?: number
    reviewMessage?: string
    reviewStatus?: number
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

  type PictureUploadRequest = {
    fileUrl?: string
    id?: number
    picName?: string
    spaceId?: number
  }

  type PictureVO = {
    category?: string
    categoryId?: string
    createTime?: string
    editTime?: string
    id?: number
    introduction?: string
    name?: string
    picColor?: string
    picFormat?: string
    picHeight?: number
    picScale?: number
    picSize?: number
    picWidth?: number
    spaceId?: number
    tagNames?: string[]
    thumbnailUrl?: string
    updateTime?: string
    url?: string
    user?: UserInfoVO
    userId?: number
  }

  type SearchPictureByColorRequest = {
    picColor?: string
    spaceId?: number
  }

  type SearchPictureByPictureRequest = {
    pictureId?: number
  }

  type Space = {
    createTime?: string
    editTime?: string
    id?: number
    isDelete?: number
    maxCount?: number
    maxSize?: number
    spaceLevel?: number
    spaceName?: string
    totalCount?: number
    totalSize?: number
    updateTime?: string
    userId?: number
  }

  type SpaceAddRequest = {
    spaceLevel?: number
    spaceName?: string
  }

  type SpaceEditRequest = {
    id?: number
    spaceName?: string
  }

  type SpaceLevel = {
    maxCount?: number
    maxSize?: number
    text?: string
    value?: number
  }

  type SpaceQueryRequest = {
    current?: number
    id?: number
    pageSize?: number
    sortFiled?: string
    sortOrder?: string
    spaceLevel?: number
    spaceName?: string
    userId?: number
  }

  type SpaceUpdateRequest = {
    id?: number
    maxCount?: number
    maxSize?: number
    spaceLevel?: number
    spaceName?: string
  }

  type SpaceVO = {
    createTime?: string
    editTime?: string
    id?: number
    maxCount?: number
    maxSize?: number
    spaceLevel?: number
    spaceName?: string
    totalCount?: number
    totalSize?: number
    updateTime?: string
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

  type UploadPictureByBatchRequest = {
    count?: number
    namePrefix?: string
    searchText?: string
  }

  type uploadPictureUsingPOSTParams = {
    fileUrl?: string
    id?: number
    picName?: string
    spaceId?: number
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
