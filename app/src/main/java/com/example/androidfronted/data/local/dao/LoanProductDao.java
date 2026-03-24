package com.example.androidfronted.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.androidfronted.data.local.entity.LoanProductEntity;
import java.util.List;

@Dao
public interface LoanProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(LoanProductEntity product);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<LoanProductEntity> products);
    
    @Update
    void update(LoanProductEntity product);
    
    @Query("SELECT * FROM loan_products")
    List<LoanProductEntity> getAllProducts();
    
    @Query("SELECT * FROM loan_products WHERE productId = :productId")
    LoanProductEntity getProductById(int productId);
    
    @Query("DELETE FROM loan_products")
    void deleteAllProducts();
    
    @Query("DELETE FROM loan_products WHERE productId = :productId")
    void deleteProductById(int productId);
}
