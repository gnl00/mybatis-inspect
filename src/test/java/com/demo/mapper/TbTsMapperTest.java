package com.demo.mapper;

import com.demo.entity.TbTs;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class TbTsMapperTest {

    @Autowired
    private TbTsMapper mapper;

    @Test
    void insertWithArray() {
        System.out.println(123);
    }

    @Test
    void selectWithArray() {
        System.out.println(mapper);
        List<TbTs> tbTs = mapper.selectWithArray();
        System.out.println(tbTs);
    }
}