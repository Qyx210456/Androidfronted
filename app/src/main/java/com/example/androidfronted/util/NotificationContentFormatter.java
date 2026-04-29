package com.example.androidfronted.util;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import androidx.core.content.ContextCompat;

import com.example.androidfronted.R;

public class NotificationContentFormatter {

    public static String getDisplayTitle(String title, String businessType, String content) {
        if (businessType == null) {
            businessType = "";
        }
        if (content == null) {
            content = "";
        }
        
        if ("REPAYMENT".equals(businessType)) {
            return "还款处理完成通知";
        }
        
        if ("LOAN_APPLICATION_STATUS".equals(businessType)) {
            if (content.contains("已提交") || content.contains("审核中")) {
                return "贷款申请状态更新";
            }
            if (content.contains("通过") && !content.contains("未通过")) {
                return "贷款申请审核通过通知";
            }
            if (content.contains("未通过") || content.contains("拒绝")) {
                return "贷款申请审核未通过通知";
            }
            if (content.contains("取消")) {
                return "贷款申请取消通知";
            }
            return "贷款申请状态更新";
        }
        
        return title != null ? title : "";
    }

    public static SpannableString formatContent(Context context, String businessType, String content, int businessId) {
        if (businessType == null) {
            businessType = "";
        }
        if (content == null) {
            content = "";
        }
        
        String fullText;
        String highlightTarget;
        int highlightColor = ContextCompat.getColor(context, R.color.primary_blue_100);
        
        if ("LOAN_APPLICATION_STATUS".equals(businessType)) {
            if (content.contains("已提交") || content.contains("审核中")) {
                highlightTarget = "贷款申请（编号：" + businessId + "）";
                fullText = "尊敬的客户：\n\n　　您的" + highlightTarget + "已成功提交，目前正处于审核阶段。我们将尽快完成审核并通知您结果。\n\n　　请您耐心等待，并留意后续通知。感谢您对我们服务的支持！";
                return highlightText(fullText, highlightTarget, highlightColor);
            }
            
            if (content.contains("通过") && !content.contains("未通过")) {
                highlightTarget = "贷款申请（编号：" + businessId + "）";
                fullText = "尊敬的客户：\n\n　　您的" + highlightTarget + "已通过审核。恭喜您，贷款申请已成功获批！\n\n　　请您根据后续指引完成相关操作。感谢您对我们服务的支持！";
                return highlightText(fullText, highlightTarget, highlightColor);
            }
            
            if (content.contains("未通过") || content.contains("拒绝")) {
                highlightTarget = "贷款申请（编号：" + businessId + "）";
                String rejectReason = extractRejectReason(content);
                fullText = "尊敬的客户：\n\n　　您的" + highlightTarget + "未通过审核。\n\n　　拒绝原因：" + rejectReason + "\n\n　　如您对审核结果有疑问，可联系我们的客服进行咨询。感谢您对我们服务的支持！";
                return highlightText(fullText, highlightTarget, highlightColor);
            }
            
            if (content.contains("取消")) {
                highlightTarget = "贷款申请（编号：" + businessId + "）";
                fullText = "尊敬的客户：\n\n　　您的" + highlightTarget + "已成功取消。\n\n　　该笔业务的相关流程已全部结束，不会产生任何费用。我们将持续为您提供优质的金融服务。感谢您对我们服务的支持！";
                return highlightText(fullText, highlightTarget, highlightColor);
            }
            
            highlightTarget = "贷款申请（编号：" + businessId + "）";
            fullText = "尊敬的客户：\n\n　　您的" + highlightTarget + "状态已更新。\n\n　　" + content + "\n\n　　感谢您对我们服务的支持！";
            return highlightText(fullText, highlightTarget, highlightColor);
        }
        
        if ("REPAYMENT".equals(businessType)) {
            highlightTarget = "还款（编号：" + businessId + "）";
            fullText = "尊敬的客户：\n\n　　您的" + highlightTarget + "已成功处理。您的还款操作已完成，账户状态已更新。\n\n　　请您留意账户变动情况。感谢您对我们服务的支持！";
            return highlightText(fullText, highlightTarget, highlightColor);
        }
        
        highlightTarget = "业务（编号：" + businessId + "）";
        fullText = "尊敬的客户：\n\n　　您的" + highlightTarget + "状态已更新。\n\n　　" + content + "\n\n　　感谢您对我们服务的支持！";
        return highlightText(fullText, highlightTarget, highlightColor);
    }

    public static String getButtonText(String businessType, int businessId) {
        if ("LOAN_APPLICATION_STATUS".equals(businessType)) {
            return "查看贷款申请（编号：" + businessId + "）";
        } else if ("REPAYMENT".equals(businessType)) {
            return "查看贷款订单（编号：" + businessId + "）";
        }
        return "查看详情";
    }

    public static boolean isApplicationRelated(String businessType) {
        return "LOAN_APPLICATION_STATUS".equals(businessType);
    }

    public static boolean isOrderRelated(String businessType) {
        return "REPAYMENT".equals(businessType);
    }

    private static SpannableString highlightText(String fullText, String targetText, int color) {
        SpannableString spannableString = new SpannableString(fullText);
        int startIndex = fullText.indexOf(targetText);
        if (startIndex != -1) {
            int endIndex = startIndex + targetText.length();
            spannableString.setSpan(
                new ForegroundColorSpan(color),
                startIndex,
                endIndex,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            spannableString.setSpan(
                new StyleSpan(Typeface.BOLD),
                startIndex,
                endIndex,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
        return spannableString;
    }

    private static String extractRejectReason(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        
        String reason = content;
        
        int rejectIndex = content.indexOf("拒绝原因：");
        if (rejectIndex != -1) {
            reason = content.substring(rejectIndex + "拒绝原因：".length());
        }
        
        reason = reason.replace("\n", "；").replace("\r", "");
        
        while (reason.contains("；；")) {
            reason = reason.replace("；；", "；");
        }
        
        if (reason.endsWith("；")) {
            reason = reason.substring(0, reason.length() - 1);
        }
        
        return reason.trim();
    }
}
