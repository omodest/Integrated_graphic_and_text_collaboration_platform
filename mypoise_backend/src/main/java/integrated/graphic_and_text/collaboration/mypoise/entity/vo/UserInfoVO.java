package integrated.graphic_and_text.collaboration.mypoise.entity.vo;

import lombok.Data;

import java.util.Date;

@Data
public class UserInfoVO {

    /**
     * id 雪花算法生成用户ID
     */
    private Long id;

    /**
     * 微信id
     */
    private Long wechat_id;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin/vip/f_vip
     */
    private String userRole;

    /**
     * 会员过期时间
     */
    private Date vip_expire;

    /**
     * 用户qq邮箱
     */
    private String email;

    /**
     * 性别
     */
    private String gender;

    /**
     * 编辑时间-可由用户控制
     */
    private Date editTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
