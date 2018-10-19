package com.oc.activiti.core.mapper;

import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @author SxL
 * Created on 10/19/2018 4:08 PM.
 */
public interface MyCustomMapper {

    /**
     * 自定义 SQL语句
     * @return List
     */
    @Select("SELECT * FROM ACT_RU_TASK")
    List<Map<String, Object>> findAll();
}
