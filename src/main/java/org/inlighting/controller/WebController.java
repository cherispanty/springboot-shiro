package org.inlighting.controller;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.*;
import org.apache.shiro.subject.Subject;

import org.inlighting.bean.UserDO;
import org.inlighting.exception.UnauthorizedException;
import org.inlighting.service.UserService;
import org.inlighting.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
public class WebController {

    private static final Logger LOGGER = LogManager.getLogger(WebController.class);

    @Autowired
    private UserService userService;


    @Autowired
    public void setService(UserService userService) {
        this.userService = userService;
    }


    @PostMapping("/login")
    public R login(@RequestParam("username") String username,
                   @RequestParam("password") String password) {
        //密码加密生成 todo
        UserDO userInfo = userService.getByUsername(username);
        if (userInfo.getPassword().equals(password)) {
            String token = JWTUtil.sign(username, password);
            //将token存入redis，设置过期时间为1分钟
            RedisUtil.set(token,userInfo,5 * 60);
            Map<String,Object> data = new HashMap<>();
            data.put("token",token);
            return R.ok(data);
        } else {
            throw new UnauthorizedException();
        }
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


}
