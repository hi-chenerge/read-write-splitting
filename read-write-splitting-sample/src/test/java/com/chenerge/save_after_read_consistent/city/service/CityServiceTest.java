package com.chenerge.save_after_read_consistent.city.service;

import com.chenerge.save_after_read_consistent.SaveAfterReadConsistentApplication;
import com.chenerge.save_after_read_consistent.city.domain.City;
import org.apache.shardingsphere.api.hint.HintManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest(classes = SaveAfterReadConsistentApplication.class)
class CityServiceTest {
    @Autowired
    private CityService cityService;

    @Test
    void findAll() {
        List<City> citys = cityService.findAll();
        System.out.println("######## All City: ");
        for (City city : citys) {
            System.out.println(city);
        }
    }

    @Test
    void save() {
        cityService.save("shandong");
        HintManager.getInstance().setMasterRouteOnly();
        List<City> citys = cityService.findAll();
        System.out.println("######## All City: ");
        System.out.println("#########master##############");
        for (City city : citys) {
            System.out.println(city);
        }
        HintManager.clear();

        System.out.println("#########master##############");
        HintManager.getInstance().setMasterRouteOnly();
        cityService.findAll();
        HintManager.clear();

        System.out.println("#########slave##############");
        cityService.findAll();
    }
}