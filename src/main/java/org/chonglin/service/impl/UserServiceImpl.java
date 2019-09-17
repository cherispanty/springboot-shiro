package org.chonglin.service.impl;

import org.chonglin.bean.UserDO;
import org.chonglin.dao.UserDao;
import org.chonglin.entity.User;
import org.chonglin.form.UserForm;
import org.chonglin.service.UserService;
import org.chonglin.util.MD5;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserDao userDao;
    @Value("${salt.login_pwd}")
    private String saltLoginPwd;

    @Override
    public UserDO getByUsername(String username) {
        return userDao.queryByUsername(username);
    }

    @Override
    public UserDO queryByColumn(Map<String,Object> map) {
        return userDao.queryByColumn(map);
    }

    @Override
    public Integer addUser(UserForm userForm) {
        //加密密码
        String text = userForm.getUsername() + userForm.getPassword();
        String cPwd = MD5.md5(text, saltLoginPwd);
        User user = new User();
        user.setUsername(userForm.getUsername());
        user.setPassword(cPwd);
        user.setBirth(userForm.getBirthday());
        return userDao.save(user);
    }
}
