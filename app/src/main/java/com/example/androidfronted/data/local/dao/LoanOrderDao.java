package com.example.androidfronted.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.androidfronted.data.local.entity.LoanOrderEntity;
import java.util.List;

@Dao
public interface LoanOrderDao {
    @Insert
    void insert(LoanOrderEntity loanOrder);

    @Insert
    void insertAll(List<LoanOrderEntity> loanOrders);

    @Query("DELETE FROM loan_orders")
    void deleteAll();

    @Query("SELECT * FROM loan_orders ORDER BY id DESC")
    List<LoanOrderEntity> getAllLoanOrders();

    @Query("SELECT * FROM loan_orders WHERE status = :status ORDER BY id DESC")
    List<LoanOrderEntity> getLoanOrdersByStatus(String status);

    @Query("SELECT * FROM loan_orders WHERE id = :id")
    LoanOrderEntity getLoanOrderById(int id);
}
