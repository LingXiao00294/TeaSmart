package com.teasmart.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.teasmart.common.BusinessException;
import com.teasmart.dto.BannerDTO;
import com.teasmart.entity.Banner;
import com.teasmart.mapper.BannerMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class BannerService {

    public static final long MAX_UPLOAD_BYTES = 5 * 1024 * 1024;
    private static final List<String> ALLOWED_EXTENSIONS = List.of(".jpg", ".jpeg", ".png", ".gif", ".webp");

    private final BannerMapper bannerMapper;

    @Value("${upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.base-path:}")
    private String basePath;

    public BannerService(BannerMapper bannerMapper) {
        this.bannerMapper = bannerMapper;
    }

    public List<Banner> listActive() {
        return bannerMapper.selectList(
                new LambdaQueryWrapper<Banner>()
                        .eq(Banner::getStatus, 1)
                        .eq(Banner::getDeleted, 0)
                        .orderByAsc(Banner::getSortOrder));
    }

    public List<Banner> listAll() {
        return bannerMapper.selectList(
                new LambdaQueryWrapper<Banner>()
                        .eq(Banner::getDeleted, 0)
                        .orderByAsc(Banner::getSortOrder));
    }

    public Banner create(BannerDTO dto) {
        Banner banner = new Banner();
        banner.setImage(dto.getImage());
        banner.setLink(dto.getLink());
        banner.setSortOrder(dto.getSortOrder());
        banner.setStatus(dto.getStatus());
        bannerMapper.insert(banner);
        return banner;
    }

    public Banner update(Long id, BannerDTO dto) {
        Banner banner = bannerMapper.selectById(id);
        if (banner == null || banner.getDeleted() == 1) {
            throw BusinessException.notFound("轮播图不存在");
        }
        banner.setImage(dto.getImage());
        banner.setLink(dto.getLink());
        banner.setSortOrder(dto.getSortOrder());
        banner.setStatus(dto.getStatus());
        bannerMapper.updateById(banner);
        return banner;
    }

    public void delete(Long id) {
        Banner banner = bannerMapper.selectById(id);
        if (banner == null || banner.getDeleted() == 1) {
            throw BusinessException.notFound("轮播图不存在");
        }
        banner.setDeleted(1);
        bannerMapper.updateById(banner);
    }

    public String upload(MultipartFile file) {
        if (file.isEmpty()) {
            throw BusinessException.badRequest("文件不能为空");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null) {
            throw BusinessException.badRequest("文件名无效");
        }

        int dot = originalName.lastIndexOf('.');
        if (dot < 0) {
            throw BusinessException.badRequest("仅支持 jpg/png/gif/webp 格式");
        }
        String ext = originalName.substring(dot).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw BusinessException.badRequest("仅支持 jpg/png/gif/webp 格式");
        }

        if (file.getSize() > MAX_UPLOAD_BYTES) {
            throw BusinessException.badRequest("文件大小不能超过5MB");
        }

        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String fileName = datePath + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8) + ext;

        Path dirPath = Paths.get(uploadDir, datePath).toAbsolutePath();
        try {
            Files.createDirectories(dirPath);
            Files.write(dirPath.resolve(fileName), file.getBytes());
        } catch (IOException e) {
            throw new BusinessException(500, "文件上传失败");
        }

        return urlPrefix(basePath) + "/uploads/" + datePath + "/" + fileName;
    }

    static String urlPrefix(String basePath) {
        if (basePath == null) return "";
        String p = basePath.trim();
        if (p.isEmpty()) return "";
        if (!p.startsWith("/")) p = "/" + p;
        if (p.endsWith("/")) p = p.substring(0, p.length() - 1);
        return p;
    }
}
