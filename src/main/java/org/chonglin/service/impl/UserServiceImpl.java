package org.chonglin.service.impl;

import org.chonglin.bean.UserDO;
import org.chonglin.dao.UserDao;
import org.chonglin.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserDao userDao;

    @Override
    public UserDO getByUsername(String username) {
        return userDao.queryByUsername(username);
    }
}
