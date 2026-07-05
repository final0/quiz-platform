package com.quiz.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class SysUser {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    /** BCrypt加密后的密码，永远不通过接口原样返回给前端 */
    private String password;
    private String realName;
    private Long deptId;
    /** ADMIN / DEPT_ADMIN / AUTHOR / STUDENT */
    private String role;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
