package com.example.androidfronted.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.androidfronted.data.local.entity.RepaymentPlanEntity;
import java.util.List;

/**
 * 还款计划数据库访问对象
 */
@Dao
public interface RepaymentPlanDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<RepaymentPlanEntity> plans);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(RepaymentPlanEntity plan);

    @Query("SELECT * FROM repayment_plans WHERE orderId = :orderId ORDER BY term ASC")
    List<RepaymentPlanEntity> getByOrderId(int orderId);

    @Query("UPDATE repayment_plans SET status = :status WHERE orderId = :orderId AND term = :term")
    void updateStatus(int orderId, int term, String status);

    @Update
    void update(RepaymentPlanEntity plan);

    @Delete
    void delete(RepaymentPlanEntity plan);

    @Query("DELETE FROM repayment_plans WHERE orderId = :orderId")
    void deleteByOrderId(int orderId);

    @Query("DELETE FROM repayment_plans")
    void deleteAll();
}
