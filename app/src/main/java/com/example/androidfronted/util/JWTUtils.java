package com.example.androidfronted.util;

import android.util.Base64;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class JWTUtils {
    
    public static Integer getUserIdFromToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        
        try {
            // JWT 格式: header.payload.signature
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return null;
            }
            
            // 解码 payload 部分
            String payload = parts[1];
            // 处理 Base64 填充
            while (payload.length() % 4 != 0) {
                payload += "=";
            }
            String decodedPayload = new String(Base64.decode(payload, Base64.URL_SAFE), StandardCharsets.UTF_8);
            
            // 解析 JSON
            JSONObject jsonObject = new JSONObject(decodedPayload);
            if (jsonObject.has("userId")) {
                try {
                    return jsonObject.getInt("userId");
                } catch (Exception e) {
                    // 如果是字符串类型，尝试转换
                    String userIdStr = jsonObject.getString("userId");
                    return Integer.parseInt(userIdStr);
                }
            } else if (jsonObject.has("sub")) {
                // 尝试从 sub 字段获取
                try {
                    return Integer.parseInt(jsonObject.getString("sub"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
}
