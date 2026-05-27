package com.teasmart.controller;

import com.teasmart.common.Result;
import com.teasmart.entity.Banner;
import com.teasmart.service.BannerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class BannerController {

    private final BannerService bannerService;

    public BannerController(BannerService bannerService) {
        this.bannerService = bannerService;
    }

    @GetMapping("/banners")
    public Result<List<Banner>> list() {
        return Result.ok(bannerService.listActive());
    }
}
