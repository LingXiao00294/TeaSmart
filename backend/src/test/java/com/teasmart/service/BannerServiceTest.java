package com.teasmart.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BannerServiceTest {

    @Test
    @DisplayName("null 输入返回空串")
    void urlPrefixNullReturnsEmpty() {
        assertThat(BannerService.urlPrefix(null)).isEmpty();
    }

    @Test
    @DisplayName("空串和纯空白返回空串")
    void urlPrefixBlankReturnsEmpty() {
        assertThat(BannerService.urlPrefix("")).isEmpty();
        assertThat(BannerService.urlPrefix("   ")).isEmpty();
    }

    @Test
    @DisplayName("标准的 /teasmart 原样返回")
    void urlPrefixStandardSlash() {
        assertThat(BannerService.urlPrefix("/teasmart")).isEqualTo("/teasmart");
    }

    @Test
    @DisplayName("缺少前导斜杠自动补")
    void urlPrefixAddsLeadingSlash() {
        assertThat(BannerService.urlPrefix("teasmart")).isEqualTo("/teasmart");
    }

    @Test
    @DisplayName("末尾斜杠被去掉")
    void urlPrefixStripsTrailingSlash() {
        assertThat(BannerService.urlPrefix("/teasmart/")).isEqualTo("/teasmart");
        assertThat(BannerService.urlPrefix("teasmart/")).isEqualTo("/teasmart");
    }

    @Test
    @DisplayName("多段路径保留中间斜杠")
    void urlPrefixMultiSegment() {
        assertThat(BannerService.urlPrefix("/a/b")).isEqualTo("/a/b");
        assertThat(BannerService.urlPrefix("/a/b/")).isEqualTo("/a/b");
    }

    @Test
    @DisplayName("拼接出来的上传 URL 形如 <prefix>/uploads/<date>/<file>")
    void uploadUrlShape() {
        String url = BannerService.urlPrefix("/teasmart") + "/uploads/20260101/abc.jpg";
        assertThat(url).isEqualTo("/teasmart/uploads/20260101/abc.jpg");
    }
}
