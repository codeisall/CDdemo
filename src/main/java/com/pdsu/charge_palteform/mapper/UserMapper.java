package com.pdsu.charge_palteform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pdsu.charge_palteform.entity.User;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface UserMapper extends BaseMapper<User> {
}

