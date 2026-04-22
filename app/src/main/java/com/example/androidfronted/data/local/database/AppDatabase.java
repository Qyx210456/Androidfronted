package com.example.androidfronted.data.local.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.example.androidfronted.data.local.dao.ApplicationDao;
import com.example.androidfronted.data.local.dao.ApplicationDetailDao;
import com.example.androidfronted.data.local.dao.AuthTokenDao;
import com.example.androidfronted.data.local.dao.CertificationDao;
import com.example.androidfronted.data.local.dao.LoanOrderDao;
import com.example.androidfronted.data.local.dao.LoanOrderDetailDao;
import com.example.androidfronted.data.local.dao.LoanProductDao;
import com.example.androidfronted.data.local.dao.NotificationDao;
import com.example.androidfronted.data.local.dao.RepaymentPlanDao;
import com.example.androidfronted.data.local.dao.UserDao;
import com.example.androidfronted.data.local.entity.ApplicationEntity;
import com.example.androidfronted.data.local.entity.ApplicationDetailEntity;
import com.example.androidfronted.data.local.entity.AuthTokenEntity;
import com.example.androidfronted.data.local.entity.CertificationEntity;
import com.example.androidfronted.data.local.entity.LoanOrderEntity;
import com.example.androidfronted.data.local.entity.LoanOrderDetailEntity;
import com.example.androidfronted.data.local.entity.LoanProductEntity;
import com.example.androidfronted.data.local.entity.NotificationEntity;
import com.example.androidfronted.data.local.entity.RepaymentPlanEntity;
import com.example.androidfronted.data.local.entity.UserEntity;

@Database(
    entities = {
        UserEntity.class,
        AuthTokenEntity.class,
        CertificationEntity.class,
        LoanProductEntity.class,
        ApplicationEntity.class,
        ApplicationDetailEntity.class,
        LoanOrderEntity.class,
        LoanOrderDetailEntity.class,
        NotificationEntity.class,
        RepaymentPlanEntity.class
    },
    version = 8,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "androidfronted_db";
    private static AppDatabase INSTANCE;

    public abstract UserDao userDao();
    public abstract AuthTokenDao authTokenDao();
    public abstract CertificationDao certificationDao();
    public abstract LoanProductDao loanProductDao();
    public abstract ApplicationDao applicationDao();
    public abstract ApplicationDetailDao applicationDetailDao();
    public abstract LoanOrderDao loanOrderDao();
    public abstract LoanOrderDetailDao loanOrderDetailDao();
    public abstract NotificationDao notificationDao();
    public abstract RepaymentPlanDao repaymentPlanDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        AppDatabase.class,
                        DATABASE_NAME
                    )
                    .fallbackToDestructiveMigration()
                    .build();
                }
            }
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        if (INSTANCE != null && INSTANCE.isOpen()) {
            INSTANCE.close();
            INSTANCE = null;
        }
    }
}
