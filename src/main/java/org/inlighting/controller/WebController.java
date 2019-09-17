package org.inlighting.controller;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.*;
import org.apache.shiro.subject.Subject;
import org.inlighting.bean.ResponseBean;

import org.inlighting.bean.UserDO;
import org.inlighting.database.UserBean;
import org.inlighting.exception.UnauthorizedException;
import org.inlighting.service.UserService;
import org.inlighting.util.Constants;
import org.inlighting.util.JWTUtil;
import org.inlighting.util.RedisUtil;
import org.inlighting.util.ShiroUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

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
    public ResponseBean login(@RequestParam("username") String username,
                              @RequestParam("password") String password) {
        //密码加密生成 todo
        UserDO userInfo = userService.getByUsername(username);
        if (userInfo.getPassword().equals(password)) {
            String token = JWTUtil.sign(username, password);
            //将token存入redis，设置过期时间为1分钟
            RedisUtil.set(token,userInfo,5 * 60);
            return new ResponseBean(200, "Login success",token);
        } else {
            throw new UnauthorizedException();
        }
    }

    @GetMapping("/test")
    public ResponseBean test(){
        return new ResponseBean(200,"test success",null);
    }

    @GetMapping("/test2")
    public ResponseBean test2(){
        return new ResponseBean(200,"you cann't attach me!",null);
    }

    /**
     * 测试获取用户信息
     * @return
     */
    @GetMapping("/getUserInfo")
    public ResponseBean getUserInfo() {
        UserDO userInfo = null;
        try {
            userInfo = ShiroUtils.getUserInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ResponseBean res = new ResponseBean(200,"success",userInfo);
        return res;
    }

    @GetMapping("/article")
    public ResponseBean article() {
        Subject subject = SecurityUtils.getSubject();
        if (subject.isAuthenticated()) {
            return new ResponseBean(200, "You are already logged in", null);
        } else {
            return new ResponseBean(200, "You are guest", null);
        }
    }

    @GetMapping("/require_auth")
    @RequiresAuthentication
    public ResponseBean requireAuth() {
        return new ResponseBean(200, "You are authenticated", null);
    }

    @GetMapping("/require_role")
    @RequiresRoles("admin")
    public ResponseBean requireRole() {
        return new ResponseBean(200, "You are visiting require_role", null);
    }

    @GetMapping("/require_permission")
    @RequiresPermissions(logical = Logical.AND, value = {"view", "edit"})
    public ResponseBean requirePermission() {
        return new ResponseBean(200, "You are visiting permission require edit,view", null);
    }

    @PostMapping("/addMenu")
    @RequiresPermissions("sys:menu:add")
    public ResponseBean addMenu() {
        return new ResponseBean(200,"add menu success",null);
    }

    @PostMapping("/addUser")
    @RequiresPermissions("sys:user:add")
    public ResponseBean addUser() {
        return new ResponseBean(200,"add user success",null);
    }

    @RequestMapping(path = "/401")
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseBean unauthorized() {
        return new ResponseBean(401, "Unauthorized", null);
    }

    @GetMapping("/logout")
    public ResponseBean logout(HttpServletRequest request) {
        String token = request.getHeader(Constants.AUTHORIZATION);
        Subject subject = SecurityUtils.getSubject();
        if (subject.isAuthenticated()) {
            //没有将登录状态保存在session中了，而是转移到redis中
//            subject.logout(); // session 会销毁，在SessionListener监听session销毁，清理权限缓存
            RedisUtil.del(token);
            LOGGER.info("退出登录成功！");
            return new ResponseBean(200,"退出成功！",null);
        }
        return new ResponseBean(401,"用户未登录！",null);
    }
}
