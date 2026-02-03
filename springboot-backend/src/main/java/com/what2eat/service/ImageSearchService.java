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

            // Step 4: 第2次AI调用 - 从多个URL中选择最佳图片
            String bestImageUrl = selectBestImageUrl(userQuery, imageUrls);
            if (bestImageUrl == null || bestImageUrl.trim().isEmpty()) {
                log.warn("AI未选择到合适的图片URL，使用第一个作为后备");
                bestImageUrl = imageUrls.get(0);
            }
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

            // 使用RestTemplate下载HTML，添加完整的浏览器请求头绕过反爬虫
            RestTemplate restTemplate = new RestTemplate();

            // 添加拦截器设置完整的请求头（模拟真实浏览器）
            restTemplate.setInterceptors(List.of(new ClientHttpRequestInterceptor() {
                @Override
                public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
                    // 基础headers
                    request.getHeaders().set("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Mobile Safari/537.36 Edg/144.0.0.0");
                    request.getHeaders().set("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
                    request.getHeaders().set("Accept-Language", "en-US,en;q=0.9,zh-CN;q=0.8");
                    // 重要：不设置Accept-Encoding，让服务器返回未压缩的HTML
                    request.getHeaders().set("Cache-Control", "max-age=0");

                    // 重要：添加Cookie绕过百度检测（使用示例Cookie，实际应从真实浏览器获取）
                    request.getHeaders().set("Cookie",
                        "BIDUPSID=8865F106810099EBFB8B76A82AFD9C1B; " +
                        "PSTM=1766108777; " +
                        "BAIDUID=8865F106810099EB1E0FF52048EA57EF:FG=1; " +
                        "H_PS_PSSID=60271_63140_66581_66592_66676_66680_66803_66850_66991_67086_67044_67111_67139_67149_67153_67161_67226_67209_67229_67241_67262_67232_67267_67244_67236; " +
                        "BAIDUID_BFESS=8865F106810099EB1E0FF52048EA57EF:FG=1"
                    );
                    request.getHeaders().set("Connection", "keep-alive");

                    // Sec-Fetch相关headers
                    request.getHeaders().set("Sec-Fetch-Dest", "document");
                    request.getHeaders().set("Sec-Fetch-Mode", "navigate");
                    request.getHeaders().set("Sec-Fetch-Site", "none");
                    request.getHeaders().set("Sec-Fetch-User", "?1");
                    request.getHeaders().set("Upgrade-Insecure-Requests", "1");

                    // Sec-Ch-Ua
                    request.getHeaders().set("Sec-Ch-Ua", "\"Not(A;Brand\";v=\"8\", \"Chromium\";v=\"144\", \"Microsoft Edge\";v=\"144\"");
                    request.getHeaders().set("Sec-Ch-Ua-Mobile", "?1");
                    request.getHeaders().set("Sec-Ch-Ua-Platform", "\"Android\"");

                    // 重要：Referer
                    request.getHeaders().set("Referer", "https://image.baidu.com/");

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

    /**
     * 第2次AI调用：从多个URL中选择最佳图片
     */
    private String selectBestImageUrl(String userQuery, List<String> imageUrls) {
        try {
            // 构建URL列表的字符串
            StringBuilder urlList = new StringBuilder();
            for (int i = 0; i < imageUrls.size() && i < 10; i++) {
                urlList.append(i + 1).append(". ").append(imageUrls.get(i)).append("\n");
            }

            String prompt = String.format("""
                用户询问: "%s"，用户想要看这道菜的成品图片。

                我从百度图片搜索中提取了以下图片URL，请帮我选择最合适的一张。

                可选图片URL:
                %s

                重要选择标准：
                1. 必须是真实的菜品照片（展示做好的菜）
                2. 坚决不要选择：logo、海报、菜单、文字图片、人物、卡通
                3. 图片尺寸要合理：
                   - 宽度应该大于高度（横版照片）
                   - 宽高比在 1.2:1 到 2:1 之间最佳
                   - 避免竖版长图（高>>宽）
                4. 优先选择中间序号的图片（3-7号），首尾结果往往不准确
                5. 只返回数字序号（1-10），不要任何解释

                请分析URL中的宽高参数（?w=XXX&h=XXX），选择最合适的菜品图片序号:
                """, userQuery, urlList.toString());

            log.info("发送给AI的URL选择prompt:\n{}", prompt);

            String response = zhipuAIService.chat(prompt);

            // 清理AI返回的内容
            if (response != null) {
                response = response.trim()
                    .replaceAll("^\"|\"$", "")
                    .replaceAll("^'|'$", "")
                    .replaceAll("\\s+", " ");

                log.info("AI选择的序号: {}", response);

                // 提取数字
                try {
                    int index = Integer.parseInt(response.replaceAll("[^0-9]", "")) - 1;
                    if (index >= 0 && index < imageUrls.size()) {
                        String selectedUrl = imageUrls.get(index);
                        log.info("AI选择的图片URL: {}", selectedUrl);
                        return selectedUrl;
                    }
                } catch (NumberFormatException e) {
                    log.warn("AI返回的不是有效数字: {}", response);
                }
            }

            return null;

        } catch (Exception e) {
            log.error("选择最佳图片URL失败", e);
            return null;
        }
    }
}
