package com.example.androidfronted.data.mapper;

import com.example.androidfronted.data.local.entity.LoanProductEntity;
import com.example.androidfronted.data.model.LoanProduct;
import com.google.gson.Gson;
import java.util.List;

public class LoanProductMapper {
    private static final Gson gson = new Gson();

    public static LoanProductEntity toEntity(LoanProduct model) {
        if (model == null) {
            return null;
        }
        return new LoanProductEntity(
                model.getProductId(),
                model.getProductName(),
                model.getDescription(),
                model.getLoanUsage(),
                model.getPromotionDetails(),
                model.getMinAmount(),
                model.getMaxAmount(),
                gson.toJson(model.getTerms()),
                gson.toJson(model.getOptions())
        );
    }

    public static LoanProduct fromEntity(LoanProductEntity entity) {
        if (entity == null) {
            return null;
        }
        LoanProduct product = new LoanProduct();
        product.setProductId(entity.getProductId());
        product.setProductName(entity.getProductName());
        product.setDescription(entity.getDescription());
        product.setLoanUsage(entity.getLoanUsage());
        product.setPromotionDetails(entity.getPromotionDetails());
        product.setMinAmount(entity.getMinAmount());
        product.setMaxAmount(entity.getMaxAmount());
        
        if (entity.getTermsJson() != null) {
            product.setTerms(gson.fromJson(entity.getTermsJson(), 
                new com.google.gson.reflect.TypeToken<List<Integer>>(){}.getType()));
        }
        
        if (entity.getOptionsJson() != null) {
            product.setOptions(gson.fromJson(entity.getOptionsJson(), 
                new com.google.gson.reflect.TypeToken<List<com.example.androidfronted.data.model.LoanProduct.LoanOption>>(){}.getType()));
        }
        
        return product;
    }
}
