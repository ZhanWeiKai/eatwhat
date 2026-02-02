package com.what2eat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 图片搜索服务 - 两步AI方案
 * 1. 第1次AI调用：生成搜图关键词
 * 2. 下载百度图片搜索HTML，提取img标签
 * 3. 第2次AI调用：从提取的URL中选择最佳图片
 */
@Service
@Slf4j
public class ImageSearchService {

    @Autowired
    private ZhipuAIService zhipuAIService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 根据用户查询搜索菜品图片
     *
     * @param userQuery 用户查询，如"我想吃红烧肉"
     * @return 最佳图片URL，失败返回null
     */
    public String searchDishImage(String userQuery) {
        try {
            log.info("开始图片搜索，用户查询: {}", userQuery);

            // Step 1: 第1次AI调用 - 生成搜图关键词
            String keywords = generateSearchKeywords(userQuery);
            if (keywords == null || keywords.trim().isEmpty()) {
                log.warn("AI未返回有效关键词");
                return null;
            }
            log.info("AI生成搜索关键词: {}", keywords);

            // Step 2: 下载百度图片搜索HTML
            String html = downloadBaiduImageSearchHtml(keywords);
            if (html == null || html.isEmpty()) {
                log.warn("无法下载百度图片搜索HTML");
                return null;
            }
            log.info("成功下载HTML，长度: {}", html.length());

            // Step 3: 使用正则表达式从HTML中提取图片URL
            List<String> imageUrls = extractImageUrlsFromHtml(html);
            if (imageUrls.isEmpty()) {
                log.warn("未能从HTML中提取到图片URL");
                return null;
            }

            log.info("从HTML中提取到 {} 个图片URL", imageUrls.size());

            // 返回第一个URL
            String bestImageUrl = imageUrls.get(0);
            log.info("选择的图片URL: {}", bestImageUrl);
            return bestImageUrl;

        } catch (Exception e) {
            log.error("图片搜索失败，用户查询: {}", userQuery, e);
            return null;
        }
    }

    /**
     * 第1次AI调用：生成搜图关键词
     */
    private String generateSearchKeywords(String userQuery) {
        try {
            String prompt = String.format("""
                用户询问: "%s"

                请根据用户的询问，生成适合在百度图片搜索中使用的关键词。

                要求:
                1. 关键词应该是简短的中文词汇或短语（2-6个字）
                2. 如果用户提到具体菜名，关键词应该是 "菜名 + 菜谱" 或 "菜名 + 成品图"
                3. 如果用户只是问"今天吃什么"，推荐一些常见家常菜
                4. 只返回关键词，不要任何解释、引号或额外文字

                示例:
                - "我想吃红烧肉" → 红烧肉菜谱
                - "推荐一道下饭菜" → 下饭菜成品图
                - "今天吃什么" → 家常菜菜谱

                关键词:
                """, userQuery);

            log.info("发送给AI的prompt:\n{}", prompt);

            String keywords = zhipuAIService.chat(prompt);

            // 清理AI返回的内容（移除可能的引号、多余空格）
            if (keywords != null) {
                keywords = keywords.trim()
                    .replaceAll("^\"|\"$", "") // 移除首尾引号
                    .replaceAll("^'|'$", "")   // 移除首尾单引号
                    .replaceAll("\\s+", " ");  // 多个空格合并为一个
            }

            return keywords;

        } catch (Exception e) {
            log.error("生成搜索关键词失败", e);
            return null;
        }
    }

    /**
     * 下载百度图片搜索HTML
     */
    private String downloadBaiduImageSearchHtml(String keywords) {
        try {
            String encodedKeywords = URLEncoder.encode(keywords, "UTF-8");
            String searchUrl = "https://image.baidu.com/search/index?tn=baiduimage&fm=result&ie=utf-8&word="
                + encodedKeywords;

            log.info("百度图片搜索URL: {}", searchUrl);

            // 使用RestTemplate下载HTML，添加浏览器请求头绕过反爬虫
            RestTemplate restTemplate = new RestTemplate();

            // 添加拦截器设置请求头
            restTemplate.setInterceptors(List.of(new ClientHttpRequestInterceptor() {
                @Override
                public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
                    request.getHeaders().set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
                    request.getHeaders().set("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
                    request.getHeaders().set("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
                    request.getHeaders().set("Referer", "https://www.baidu.com/");
                    return execution.execute(request, body);
                }
            }));

            String html = restTemplate.getForObject(searchUrl, String.class);

            if (html != null) {
                log.info("成功下载HTML，长度: {}", html.length());
                // Debug: 打印HTML前2000字符用于调试
                String preview = html.length() > 2000 ? html.substring(0, 2000) : html;
                log.info("HTML预览（前2000字符）: {}", preview);
                return html;
            }

        } catch (Exception e) {
            log.error("下载百度图片搜索HTML失败", e);
        }

        return null;
    }

    /**
     * 使用正则表达式从HTML中提取图片URL
     */
    private List<String> extractImageUrlsFromHtml(String html) {
        List<String> urls = new ArrayList<>();

        try {
            // 更宽松的正则：搜索所有包含 baidu.com/it/u= 的URL
            // 不限制必须在img标签中，因为百度可能使用data-imgurl等属性
            Pattern pattern = Pattern.compile(
                "https?://img[0-3]\\.baidu\\.com/it/u=[0-9]+,[0-9]+[^\"><]*?\\?[wh]=[0-9]+[^\"><]*",
                Pattern.CASE_INSENSITIVE
            );

            Matcher matcher = pattern.matcher(html);
            while (matcher.find()) {
                String url = matcher.group(0);

                // 清理URL：移除可能的HTML实体和Unicode转义
                url = url.replace("&amp;", "&");
                url = url.replace("\\u0026", "&");
                url = url.replace("\\\"", "\"");  // 移除转义的引号

                urls.add(url);
                log.info("提取到图片URL: {}", url);

                // 只取前10个
                if (urls.size() >= 10) {
                    break;
                }
            }

            log.info("从HTML中提取到 {} 个百度图片URL", urls.size());

        } catch (Exception e) {
            log.error("从HTML中提取图片URL失败", e);
        }

        return urls;
    }
}
