package org.inlighting;

import org.inlighting.database.UserBean;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DemoApplicationTests {
    @Autowired
    RedisTemplate redisTemplate;

    @Test
    public void contextLoads() {

    }

    @Test
    public void testRedis() {
        UserBean user = new UserBean();
        user.setPassword("123456");
        user.setUsername("admin123");
        user.setPermission("sys:user:list");
        user.setRole("admin");
        redisTemplate.opsForValue().set("user",user);
        UserBean obj = (UserBean) redisTemplate.opsForValue().get("user");

        System.out.println(obj.getPassword());
        System.out.println(obj.getPermission());
        System.out.println(obj.getRole());
        System.out.println(obj.getUsername());
    }

}
