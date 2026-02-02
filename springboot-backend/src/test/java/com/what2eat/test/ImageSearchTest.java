package com.what2eat.test;

import com.what2eat.service.ZhipuAIService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 百度图片搜索HTML提取测试
 */
@SpringBootTest
public class ImageSearchTest {

    @Autowired
    private ZhipuAIService zhipuAIService;

    /**
     * 测试第1次AI调用：生成搜图关键词
     */
    @Test
    public void testGenerateSearchKeywords() {
        String userQuery = "我想吃红烧肉";

        try {
            // 构建专门的Prompt让AI生成搜索关键词
            String prompt = String.format("""
                用户询问: "%s"

                请根据用户的询问，生成适合在百度图片搜索中使用的关键词。

                要求:
                1. 关键词应该是简短的中文词汇或短语
                2. 如果用户提到具体菜名，关键词应该是 "菜名 + 菜谱" 或 "菜名 + 成品图"
                3. 如果用户只是问"今天吃什么"，推荐一些常见家常菜
                4. 只返回关键词，不要任何解释或额外文字

                示例:
                - "我想吃红烧肉" → "红烧肉 菜谱"
                - "推荐一道下饭菜" → "下饭菜 成品图"
                - "今天吃什么" → "家常菜 菜谱"

                关键词:
                """, userQuery);

            System.out.println("发送AI请求，生成搜索关键词...");
            String keywords = zhipuAIService.chat(prompt);

            System.out.println("✅ AI生成的搜索关键词: " + keywords);

            // 验证关键词不为空且长度合理
            if (keywords == null || keywords.trim().isEmpty()) {
                System.out.println("❌ AI返回空关键词");
                return;
            }

            if (keywords.length() > 50) {
                System.out.println("⚠️ 警告: 关键词过长(" + keywords.length() + "字符)，可能需要精简");
            } else {
                System.out.println("✓ 关键词长度适中");
            }

        } catch (Exception e) {
            System.err.println("❌ 生成搜索关键词失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 测试下载百度图片搜索页面并提取img标签
     */
    @Test
    public void testBaiduImageSearchExtraction() {
        // 搜索关键词
        String keyword = "红烧肉";

        try {
            // 构建百度图片搜索URL（URL编码）
            String encodedKeyword = java.net.URLEncoder.encode(keyword, "UTF-8");
            String searchUrl = "https://image.baidu.com/search/index?tn=baiduimage&fm=result&ie=utf-8&word=" + encodedKeyword;

            System.out.println("搜索URL: " + searchUrl);

            // 下载HTML，添加必要的headers
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getInterceptors().add(new ClientHttpRequestInterceptor() {
                @Override
                public ClientHttpResponse intercept(org.springframework.http.HttpRequest request,
                                                    byte[] body,
                                                    ClientHttpRequestExecution execution) throws IOException {
                    HttpHeaders headers = request.getHeaders();
                    headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
                    headers.set("Referer", "https://image.baidu.com");
                    headers.set("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
                    return execution.execute(request, body);
                }
            });

            String html = restTemplate.getForObject(searchUrl, String.class);

            if (html == null || html.isEmpty()) {
                System.out.println("❌ HTML下载失败");
                return;
            }

            System.out.println("✅ HTML下载成功，长度: " + html.length());

            // 提取img标签中的src属性（支持单引号和双引号）
            Pattern pattern = Pattern.compile("<img[^>]*src=[\"']([^\"'<>]+)[\"'][^>]*>", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(html);

            System.out.println("\n找到的图片URL:");
            int count = 0;
            int baiduCount = 0;
            while (matcher.find() && count < 20) { // 显示前20个
                String src = matcher.group(1);
                count++;

                // 清理URL：移除可能的参数
                if (src.contains("?")) {
                    src = src.substring(0, src.indexOf("?"));
                }

                System.out.println(count + ". " + src);

                // 验证是否是百度CDN的图片
                if (src.contains("img1.baidu.com") || src.contains("img2.baidu.com") || src.contains("img0.baidu.com")) {
                    System.out.println("   ✓ 这是百度CDN图片");
                    baiduCount++;
                }
            }

            System.out.println("\n总共找到: " + count + " 个图片URL");
            System.out.println("其中百度CDN图片: " + baiduCount + " 个");

        } catch (Exception e) {
            System.err.println("❌ 发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
