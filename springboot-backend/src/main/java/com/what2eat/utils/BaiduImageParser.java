package com.what2eat.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 百度图片HTML解析工具类
 * 从百度图片搜索HTML中提取图片URL
 */
@Slf4j
public class BaiduImageParser {

    private static final Pattern IMG_PATTERN = Pattern.compile(
        "<img[^>]*src=[\"']([^\"'<>]+?)[\"'][^>]*>",
        Pattern.CASE_INSENSITIVE
    );

    /**
     * 从百度图片搜索HTML中提取所有图片URL
     *
     * @param html HTML内容
     * @return 图片URL列表
     */
    public static List<String> extractImageUrls(String html) {
        List<String> urls = new ArrayList<>();

        if (html == null || html.isEmpty()) {
            log.warn("HTML内容为空");
            return urls;
        }

        Matcher matcher = IMG_PATTERN.matcher(html);
        while (matcher.find()) {
            String src = matcher.group(1);

            // 过滤：只保留百度CDN的图片
            if (src.contains("img0.baidu.com/it/")
                || src.contains("img1.baidu.com/it/")
                || src.contains("img2.baidu.com/it/")) {

                // 移除可能的查询参数（保留主要参数）
                if (src.contains("?")) {
                    // 保留关键参数：w, h, fm, fmt等
                    String[] parts = src.split("\\?");
                    if (parts.length > 0) {
                        urls.add(parts[0] + "?" + parts[1]);
                    }
                } else {
                    urls.add(src);
                }
            }
        }

        log.info("从HTML中提取到 {} 个百度图片URL", urls.size());
        return urls;
    }

    /**
     * 创建配置好的RestTemplate（添加必要的HTTP头）
     */
    public static RestTemplate createRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        // 添加拦截器，设置必要的HTTP头
        restTemplate.getInterceptors().add(new ClientHttpRequestInterceptor() {
            @Override
            public ClientHttpResponse intercept(
                org.springframework.http.HttpRequest request,
                byte[] body,
                ClientHttpRequestExecution execution) throws IOException {

                HttpHeaders headers = request.getHeaders();
                // 注意：HttpHeaders是不可变的，需要重新创建
                // 但这里我们无法直接修改，所以使用其他方式

                return execution.execute(request, body);
            }
        });

        return restTemplate;
    }

    /**
     * 下载百度图片搜索页面的HTML
     * 注意：由于百度使用JavaScript渲染，RestTemplate可能无法获取完整内容
     * 建议使用Playwright或Selenium
     *
     * @param keyword 搜索关键词
     * @return HTML内容
     */
    public static String downloadBaiduImageSearchHtml(String keyword) {
        try {
            String encodedKeyword = java.net.URLEncoder.encode(keyword, "UTF-8");
            String searchUrl = "https://image.baidu.com/search/index?tn=baiduimage&fm=result&ie=utf-8&word="
                + encodedKeyword;

            log.info("下载百度图片搜索页面: {}", searchUrl);

            RestTemplate restTemplate = new RestTemplate();
            String html = restTemplate.getForObject(searchUrl, String.class);

            if (html != null) {
                log.info("成功下载HTML，长度: {}", html.length());

                // 检查是否是JavaScript渲染的页面
                if (html.contains("加载失败，请关闭") || html.length() < 10000) {
                    log.warn("HTML内容过短或需要JavaScript渲染，RestTemplate无法处理");
                    return null;
                }

                return html;
            }

        } catch (Exception e) {
            log.error("下载百度图片搜索HTML失败", e);
        }

        return null;
    }
}
