package com.chenerge.save_after_read_consistent.city.service;

import com.chenerge.save_after_read_consistent.city.domain.City;
import com.chenerge.save_after_read_consistent.city.mapper.CityMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CityService {
    @Autowired
    private CityMapper cityMapper;

    public List<City> findAll(){
        return cityMapper.findAll();
    }

    @Transactional
    public boolean save(String name){
        int save = cityMapper.save(name);
        return save == 1;
    }

}
