package com.demo.mapper;

import com.demo.entity.TbTs;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface TbTsMapper {
    int insertWithArray(@Param("timestamp") Date timestamp, @Param("id") Integer id, @Param("arr") Integer[] arr, @Param("arrFl") Float[] arrFl);

    List<TbTs> selectWithArray();
}
