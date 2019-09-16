package org.inlighting.dao;

import org.apache.ibatis.annotations.Mapper;
import org.inlighting.bean.UserDO;

/**
 * 
 * @author chglee
 * @email 1992lcg@163.com
 * @date 2017-10-03 09:45:11
 */
@Mapper
public interface UserDao {
    UserDO queryByUsername(String username);
}
