package com.demo.mapper;

import com.demo.entity.TbTs;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;
import java.util.List;

@Mapper
public interface TbTsMapper {
    int insertWithArray(Date timestamp, Integer id, Integer[] arr, Float[] arrFl);

    List<TbTs> selectWithArray();
}
