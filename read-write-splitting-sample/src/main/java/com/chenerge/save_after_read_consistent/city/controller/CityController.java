package com.chenerge.save_after_read_consistent.city.controller;

import com.chenerge.save_after_read_consistent.city.common.interceptor.EnableMasterReadLater;
import com.chenerge.save_after_read_consistent.city.common.interceptor.MasterReadIfNeeded;
import com.chenerge.save_after_read_consistent.city.domain.City;
import com.chenerge.save_after_read_consistent.city.service.CityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CityController {
    @Autowired
    private CityService cityService;

    /**
     * 查询city
     * 只会走从库
     * @return
     */
    @GetMapping("/city/findAll0")
    public List<City> findAll0() {
        return cityService.findAll();
    }

    /**
     * 查询全部city
     * 可能会走主库
     * @return
     */
    @MasterReadIfNeeded
    @GetMapping("/city/findAll")
    public List<City> findAll() {
        return cityService.findAll();
    }

    /**
     * 保存city
     * 后续查询会走5次主库
     * @param name
     * @return
     */
    @EnableMasterReadLater(readMasterCnt = 3)
    @PostMapping("/city/save")
    public String save(@RequestParam("name") String name) {
        cityService.save(name);
        return "ok";
    }
}
