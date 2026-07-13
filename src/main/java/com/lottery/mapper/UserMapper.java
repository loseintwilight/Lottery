package com.lottery.mapper;

import com.lottery.pojo.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface UserMapper {

    int insert(User user);

    int batchInsertUsers(@Param("list") List<User> users);

    User selectByUsername(String username);

    User selectById(Integer id);

    List<User> selectByIds(@Param("ids") List<Integer> ids);

    int addBalance(@Param("id") Integer id, @Param("amount") BigDecimal amount);

    int deductBalance(@Param("id") Integer id, @Param("amount") BigDecimal amount);

    int deleteAll();

    Integer selectMaxId();
}
