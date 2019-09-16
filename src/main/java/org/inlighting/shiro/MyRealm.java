package org.inlighting.shiro;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

import org.inlighting.bean.MenuDO;
import org.inlighting.bean.RoleDO;
import org.inlighting.bean.UserDO;
import org.inlighting.database.UserBean;
import org.inlighting.service.UserService;
import org.inlighting.util.JWTUtil;
import org.inlighting.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MyRealm extends AuthorizingRealm {

    private static final Logger LOGGER = LogManager.getLogger(MyRealm.class);

    @Autowired
    private UserService userService;

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    /**
     * 大坑！，必须重写此方法，不然Shiro会报错
     */
    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JWTToken;
    }

    /**
     * 只有当需要检测用户权限的时候才会调用此方法，例如checkRole,checkPermission之类的
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        String username = JWTUtil.getUsername(principals.toString());
//        UserBean user = userService.getUser(username);
        UserDO userDO = userService.getByUsername(username);
        SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
//        simpleAuthorizationInfo.addRole(user.getRole());
        List<RoleDO> roleDOList = userDO.getRoleDOList();
        List<String> roles = roleDOList.stream().map(RoleDO::getRoleSign).collect(Collectors.toList());
        Set<String> permission = new HashSet<>();
        simpleAuthorizationInfo.addRoles(roles);
        for (RoleDO role: roleDOList) {
            //去掉为空的权限
            Set<String> set = role.getMenuDOList().stream().filter(item -> !StringUtils.isEmpty(item.getPerms())).map(MenuDO::getPerms).collect(Collectors.toSet());
            permission.addAll(set);
        }
        simpleAuthorizationInfo.addStringPermissions(permission);
        return simpleAuthorizationInfo;
    }

    /**
     * 默认使用此方法进行用户名正确与否验证，错误抛出异常即可。
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken auth) throws AuthenticationException {
        String token = (String) auth.getCredentials();
        // 解密获得username，用于和数据库进行对比
        String username = JWTUtil.getUsername(token);
        if (username == null) {
            throw new AuthenticationException("token invalid");
        }
        //从redis查询token记录
        UserDO userDO = (UserDO) RedisUtil.get(token);
//        UserBean userBean = userService.getUser(username);
        if (userDO == null) {
            throw new AuthenticationException("User didn't existed!");
        }

        if (! JWTUtil.verify(token, username, userDO.getPassword())) {
            throw new AuthenticationException("Username or password error");
        }

        return new SimpleAuthenticationInfo(token, token, "my_realm");
    }
}
