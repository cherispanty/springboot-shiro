package org.chonglin.form;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.Date;


@Data
public class UserForm {
    @NotBlank(message = "用户名不能为空")
    private String username;
    @NotBlank(message = "密码不能为空")
    private String password;
    @NotBlank(message = "校验码不能为空")
    private String code;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") //格式化前台日期参数注解
    private Date birthday;
}
