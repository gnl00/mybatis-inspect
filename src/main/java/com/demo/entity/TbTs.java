package com.demo.entity;

import com.demo.anno.TypeHandler;
import com.demo.handler.ArrayTypeHandler;
import lombok.Data;

import java.util.Date;

/**
 drop table tb_ts;
 create table tb_ts(
     time timestamp not null,
     id int,
     arr int[],
     arr_fl float4[]
 );
 */
@Data
public class TbTs {

    Date time;

    Integer id;

    @TypeHandler(ArrayTypeHandler.class)
    Integer[] arr;

    /*
     java float[] 对应 postgresql float4[]
     java double[] 对应 postgresql float8[]
     */
    @TypeHandler(ArrayTypeHandler.class)
    Float[] arrFl;
}
