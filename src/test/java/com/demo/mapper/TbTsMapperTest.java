package com.demo.mapper;

import com.demo.entity.TbTs;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

@SpringBootTest
class TbTsMapperTest {

    @Autowired
    private TbTsMapper mapper;

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Test
    void insertSqlSessionFactory() {
        Assert.notNull(sqlSessionFactory);
        System.out.println(sqlSessionFactory);
        // sqlSessionFactory.getConfiguration().addInterceptor(new TypeHandlerPlugin());
    }

    @Test
    void insertWithArray() {
        mapper.insertWithArray(new Date(), 1005, new Integer[]{1, 2, 3, 4}, new Float[]{1f, 2f, 3f, 4f});
    }

    @Test
    void selectWithArray() {
        System.out.println(mapper);
        List<TbTs> tbTs = mapper.selectWithArray();
        System.out.println(tbTs);
    }
}