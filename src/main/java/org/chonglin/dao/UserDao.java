package org.chonglin.dao;

import org.apache.ibatis.annotations.Mapper;
import org.chonglin.bean.UserDO;
import org.chonglin.entity.User;

import java.util.Map;

/**
 * 
 * @author chglee
 * @email 1992lcg@163.com
 * @date 2017-10-03 09:45:11
 */
@Mapper
public interface UserDao {
    UserDO queryByUsername(String username);

    UserDO queryByColumn(Map<String,Object> map);

    Integer save(User user);
}
