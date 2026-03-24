package com.liang.shituapi.controller;

import com.liang.shituapi.service.AmapStaticMapService;
import com.liang.shitucommon.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 静态地图控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/staticmap")
public class StaticMapController {

    @Autowired
    private AmapStaticMapService staticMapService;

    /**
     * 获取单点静态地图URL
     */
    @GetMapping("/single")
    public Result<String> getSingleMap(
            @RequestParam BigDecimal longitude,
            @RequestParam BigDecimal latitude,
            @RequestParam(defaultValue = "15") int zoom,
            @RequestParam(defaultValue = "400") int width,
            @RequestParam(defaultValue = "300") int height) {

        String url = staticMapService.getStaticMapUrl(longitude, latitude, zoom, width, height);
        return Result.success(url);
    }
}
