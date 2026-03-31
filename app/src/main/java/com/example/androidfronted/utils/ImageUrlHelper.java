package com.example.androidfronted.utils;

/**
 * 图片 URL 拼接工具类
 * 用于将接口返回的相对路径拼接成完整的网络图片 URL
 */
public class ImageUrlHelper {
    // 服务器基础 URL（模拟器访问本地服务器）
    private static final String BASE_URL = "http://10.0.2.2:8080";
    
    /**
     * 将相对路径拼接成完整的图片 URL
     * @param relativePath 相对路径，例如：/uploads/work/employment/employment_10_20260311_cz77r0.png
     * @return 完整的图片 URL，例如：http://10.0.2.2:8080/uploads/work/employment/employment_10_20260311_cz77r0.png
     */
    public static String getFullImageUrl(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return null;
        }
        
        // 如果已经是完整的 URL，则直接返回
        if (relativePath.startsWith("http://") || relativePath.startsWith("https://")) {
            return relativePath;
        }
        
        // 拼接完整的 URL
        return BASE_URL + relativePath;
    }
}