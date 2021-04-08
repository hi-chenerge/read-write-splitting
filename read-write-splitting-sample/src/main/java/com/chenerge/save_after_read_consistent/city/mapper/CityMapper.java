package com.chenerge.save_after_read_consistent.city.mapper;

import com.chenerge.save_after_read_consistent.city.domain.City;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CityMapper {
    @Select("select id, name from city")
    List<City> findAll();

    @Insert("insert into city (name) values( #{name} )")
    int save(@Param("name") String name);
}
