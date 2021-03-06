package org.chonglin.dao;

import org.chonglin.bean.UserDO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest

public class UserDaoTest {
    @Autowired
    private UserDao userDao;

    @Test
    public void queryByUsername() {
        UserDO userDO = userDao.queryByUsername("smith");
        System.out.println(userDO);
    }
}