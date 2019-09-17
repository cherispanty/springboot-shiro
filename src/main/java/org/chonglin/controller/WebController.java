package org.chonglin.controller;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.*;
import org.apache.shiro.subject.Subject;

import org.chonglin.bean.UserDO;
import org.chonglin.exception.UnauthorizedException;
import org.chonglin.form.UserForm;
import org.chonglin.service.UserService;
import org.chonglin.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
public class WebController {

    private static final Logger LOGGER = LogManager.getLogger(WebController.class);

    @Autowired
    private UserService userService;
    @Value("${salt.login_pwd}")
    private String saltLoginPwd;


    @PostMapping("/login")
    public R login(@RequestParam(value = "username") String username,
                   @RequestParam("password") String password) {
        //密码加密生成
        UserDO userInfo = userService.getByUsername(username);
        String text = username + password;
        String cPwd = MD5.md5(text, saltLoginPwd);
        if(!userInfo.getPassword().equals(cPwd)) {
            return R.error("用户名或密码错误");
        }
        String token = JWTUtil.sign(username, cPwd);
        //将token存入redis，设置过期时间为1分钟
        RedisUtil.set(token,userInfo,5 * 60);
        Map<String,Object> data = new HashMap<>();
        data.put("token",token);
        return R.ok(data);

    }

    @GetMapping("/test")
    public R test(){
        return R.ok();
    }

    @GetMapping("/test2")
    public R test2(){
        return R.ok();
    }

    /**
     * 测试获取用户信息
     * @return
     */
    @GetMapping("/getUserInfo")
    @RequiresAuthentication
    public R getUserInfo() {
        UserDO userInfo = ShiroUtils.getUserInfo();
        return R.ok().put("data",userInfo);
    }

    @GetMapping("/article")
    public R article() {
        Subject subject = SecurityUtils.getSubject();
        if (subject.isAuthenticated()) {
            return R.ok("You are already logged in");
        } else {
            return R.error("You are guest");
        }
    }

    @GetMapping("/require_auth")
    @RequiresAuthentication
    public R requireAuth() {
        return R.ok("You are authenticated");
    }

    @GetMapping("/require_role")
    @RequiresRoles("admin")
    public R requireRole() {
        return R.ok("You are visiting require_role");
    }

    @GetMapping("/require_permission")
    @RequiresPermissions(logical = Logical.AND, value = {"view", "edit"})
    public R requirePermission() {
        return R.ok("You are visiting permission require edit,view");
    }

    @PostMapping("/addMenu")
    @RequiresPermissions("sys:menu:add")
    public R addMenu() {
        return R.ok("add menu success");
    }

    @PostMapping("/addUser")
    @RequiresPermissions("sys:user:add")
    public R addUser() {
        return R.ok("add user success");
    }

    @RequestMapping(path = "/401")
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public R unauthorized() {
        return R.error("401","Unauthorized");
    }

    @GetMapping("/logout")
    public R logout(HttpServletRequest request) {
        String token = request.getHeader(Constants.AUTHORIZATION);
        Subject subject = SecurityUtils.getSubject();
        if (subject.isAuthenticated()) {
            //没有将登录状态保存在session中了，而是转移到redis中
//            subject.logout(); // session 会销毁，在SessionListener监听session销毁，清理权限缓存
            RedisUtil.del(token);
            LOGGER.info("退出登录成功！");
            return R.ok("退出成功");
        }
        return R.error("用户未登录");
    }

    @PostMapping("/regist")
    @RequiresAuthentication
    public R regist(@RequestBody @Valid UserForm userForm) {
        //检查用户是否存在
        Map<String,Object> map = new HashMap<>();
        map.put("username",userForm.getUsername());
        UserDO userDO = userService.queryByColumn(map);
        if(userDO != null) {
            return R.error("该用户名已注册");
        }
        Integer row = userService.addUser(userForm);
        if(row != 1) {
            return R.error("注册失败！");
        }
        return R.ok("注册成功！");
    }


}
