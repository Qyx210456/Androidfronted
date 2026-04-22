package com.example.androidfronted.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.androidfronted.data.local.entity.LoanOrderDetailEntity;

@Dao
public interface LoanOrderDetailDao {
    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    void insert(LoanOrderDetailEntity loanOrderDetail);

    @Query("SELECT * FROM loan_order_details WHERE id = :id")
    LoanOrderDetailEntity getById(int id);

    @Query("DELETE FROM loan_order_details")
    void deleteAll();

    @Query("DELETE FROM loan_order_details WHERE id = :id")
    void deleteById(int id);

    @Query("UPDATE loan_order_details SET currentTerm = :newCurrentTerm WHERE id = :orderId")
    void updateCurrentTerm(int orderId, int newCurrentTerm);
}
