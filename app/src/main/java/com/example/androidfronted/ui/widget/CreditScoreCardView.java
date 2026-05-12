package com.example.androidfronted.ui.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.androidfronted.R;
import com.example.androidfronted.util.CreditLevelUtil;

import eightbitlab.com.blurview.BlurView;

public class CreditScoreCardView extends FrameLayout {

    private FrameLayout flBackground;
    private BlurView blurView;
    private ImageView ivBadge;
    private ImageView ivCrown;
    private ImageView ivArrow;
    private TextView tvTitle;
    private TextView tvSubtitle;
    private TextView tvScoreNumber;
    private TextView tvScoreUnit;
    private TextView tvScoreRemaining;
    private TextView tvLevelMax;
    private ProgressBar progressBar;

    private int creditScore = 0;
    private OnCardClickListener onCardClickListener;
    private boolean blurSetup = false;

    public interface OnCardClickListener {
        void onCardClick(int creditScore, int level);
    }

    public CreditScoreCardView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public CreditScoreCardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CreditScoreCardView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.layout_credit_score_card, this, true);
        
        initView();
    }

    private void initView() {
        flBackground = findViewById(R.id.fl_background);
        blurView = findViewById(R.id.blur_view);
        ivBadge = findViewById(R.id.iv_badge);
        ivCrown = findViewById(R.id.iv_crown);
        ivArrow = findViewById(R.id.iv_arrow);
        tvTitle = findViewById(R.id.tv_title);
        tvSubtitle = findViewById(R.id.tv_subtitle);
        tvScoreNumber = findViewById(R.id.tv_score_number);
        tvScoreUnit = findViewById(R.id.tv_score_unit);
        tvScoreRemaining = findViewById(R.id.tv_score_remaining);
        tvLevelMax = findViewById(R.id.tv_level_max);
        progressBar = findViewById(R.id.progress_bar);
    }

    public void setupBlurView(ViewGroup rootView) {
        if (blurSetup || blurView == null) return;
        
        blurView.setupWith(rootView)
                .setBlurEnabled(true)
                .setBlurRadius(25f)
                .setBlurAutoUpdate(true);
        
        blurSetup = true;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!blurSetup) {
            if (getRootView() instanceof ViewGroup) {
                setupBlurView((ViewGroup) getRootView());
            }
        }
    }

    public void setCreditScore(int score) {
        this.creditScore = score;
        updateCard();
    }

    public int getCreditScore() {
        return creditScore;
    }

    private void updateCard() {
        setVisibility(VISIBLE);
        
        int level = CreditLevelUtil.getLevel(creditScore);
        int levelMax = CreditLevelUtil.getLevelMax(level);
        int remaining = levelMax - creditScore;
        int progress = (int) (creditScore * 100.0 / levelMax);
        
        tvScoreNumber.setText(String.valueOf(creditScore));
        tvLevelMax.setText(levelMax + " 信誉分");
        
        if (level == CreditLevelUtil.LEVEL_ELITE) {
            tvScoreRemaining.setText(" 已达到最高等级");
        } else {
            tvScoreRemaining.setText(" 还差" + remaining + "信誉分可升级");
        }
        
        progressBar.setProgress(progress);
        
        updateCardByLevel(level);
    }

    private void updateCardByLevel(int level) {
        Context context = getContext();
        
        switch (level) {
            case CreditLevelUtil.LEVEL_BASIC:
                flBackground.setBackgroundResource(R.drawable.bg_credit_score_base_card);
                ivBadge.setImageResource(R.drawable.ic_credit_score_card_basic_badge);
                ivCrown.setImageResource(R.drawable.ic_credit_score_card_basic_crown);
                ivArrow.setImageResource(R.drawable.ic_credit_score_card_arrow_right_base);
                tvTitle.setText("基础信誉");
                tvTitle.setTextColor(context.getColor(R.color.credit_basic_text_primary));
                tvSubtitle.setText("夯实信用基石，完善个人数据");
                tvSubtitle.setTextColor(context.getColor(R.color.credit_basic_text_secondary));
                tvScoreNumber.setTextColor(context.getColor(R.color.credit_basic_text_secondary));
                tvScoreUnit.setTextColor(context.getColor(R.color.credit_basic_text_secondary));
                tvScoreRemaining.setTextColor(context.getColor(R.color.credit_basic_text_secondary));
                tvLevelMax.setTextColor(context.getColor(R.color.credit_basic_text_secondary));
                progressBar.setProgressDrawable(context.getDrawable(R.drawable.progress_bar_credit_base));
                break;
                
            case CreditLevelUtil.LEVEL_STEADY:
                flBackground.setBackgroundResource(R.drawable.bg_credit_score_stable_card);
                ivBadge.setImageResource(R.drawable.ic_credit_score_card_steady_badge);
                ivCrown.setImageResource(R.drawable.ic_credit_score_card_steady_crown);
                ivArrow.setImageResource(R.drawable.ic_credit_score_card_arrow_right_stable);
                tvTitle.setText("稳健信誉");
                tvTitle.setTextColor(context.getColor(R.color.credit_steady_text_primary));
                tvSubtitle.setText("脱离新手阶段，行为稳定可控");
                tvSubtitle.setTextColor(context.getColor(R.color.credit_steady_text_secondary));
                tvScoreNumber.setTextColor(context.getColor(R.color.credit_steady_text_secondary));
                tvScoreUnit.setTextColor(context.getColor(R.color.credit_steady_text_secondary));
                tvScoreRemaining.setTextColor(context.getColor(R.color.credit_steady_text_secondary));
                tvLevelMax.setTextColor(context.getColor(R.color.credit_steady_text_secondary));
                progressBar.setProgressDrawable(context.getDrawable(R.drawable.progress_bar_credit_steady));
                break;
                
            case CreditLevelUtil.LEVEL_GOOD:
                flBackground.setBackgroundResource(R.drawable.bg_credit_score_good_card);
                ivBadge.setImageResource(R.drawable.ic_credit_score_card_good_badge);
                ivCrown.setImageResource(R.drawable.ic_credit_score_card_good_crown);
                ivArrow.setImageResource(R.drawable.ic_credit_score_card_arrow_right_good);
                tvTitle.setText("良好信誉");
                tvTitle.setTextColor(context.getColor(R.color.credit_good_text_primary));
                tvSubtitle.setText("信用状态健康，达到活跃标准");
                tvSubtitle.setTextColor(context.getColor(R.color.credit_good_text_secondary));
                tvScoreNumber.setTextColor(context.getColor(R.color.credit_good_text_tertiary));
                tvScoreUnit.setTextColor(context.getColor(R.color.credit_good_text_tertiary));
                tvScoreRemaining.setTextColor(context.getColor(R.color.credit_good_text_tertiary));
                tvLevelMax.setTextColor(context.getColor(R.color.credit_good_text_tertiary));
                progressBar.setProgressDrawable(context.getDrawable(R.drawable.progress_bar_credit_good));
                break;
                
            case CreditLevelUtil.LEVEL_EXCELLENT:
                flBackground.setBackgroundResource(R.drawable.bg_credit_score_excellent_card);
                ivBadge.setImageResource(R.drawable.ic_credit_score_card_excellent_badge);
                ivCrown.setImageResource(R.drawable.ic_credit_score_card_excellent_crown);
                ivArrow.setImageResource(R.drawable.ic_credit_score_card_arrow_right_excellent);
                tvTitle.setText("优秀信誉");
                tvTitle.setTextColor(context.getColor(R.color.credit_excellent_text_primary));
                tvSubtitle.setText("超越普通水平，彰显优质价值");
                tvSubtitle.setTextColor(context.getColor(R.color.credit_excellent_text_secondary));
                tvScoreNumber.setTextColor(context.getColor(R.color.credit_excellent_text_secondary));
                tvScoreUnit.setTextColor(context.getColor(R.color.credit_excellent_text_secondary));
                tvScoreRemaining.setTextColor(context.getColor(R.color.credit_excellent_text_secondary));
                tvLevelMax.setTextColor(context.getColor(R.color.credit_excellent_text_secondary));
                progressBar.setProgressDrawable(context.getDrawable(R.drawable.progress_bar_credit_excellent));
                break;
                
            case CreditLevelUtil.LEVEL_ELITE:
                flBackground.setBackgroundResource(R.drawable.bg_credit_score_elite_card);
                ivBadge.setImageResource(R.drawable.ic_credit_score_card_superior_badge);
                ivCrown.setImageResource(R.drawable.ic_credit_score_card_superior_crown);
                ivArrow.setImageResource(R.drawable.ic_credit_score_card_arrow_right_top);
                tvTitle.setText("卓越信誉");
                tvTitle.setTextColor(context.getColor(R.color.credit_elite_text_primary));
                tvSubtitle.setText("表现无可挑剔，跻身塔尖用户");
                tvSubtitle.setTextColor(context.getColor(R.color.credit_elite_text_secondary));
                tvScoreNumber.setTextColor(context.getColor(R.color.credit_elite_text_secondary));
                tvScoreUnit.setTextColor(context.getColor(R.color.credit_elite_text_secondary));
                tvScoreRemaining.setTextColor(context.getColor(R.color.credit_elite_text_secondary));
                tvLevelMax.setTextColor(context.getColor(R.color.credit_elite_text_secondary));
                progressBar.setProgressDrawable(context.getDrawable(R.drawable.progress_bar_credit_elite));
                break;
        }
    }

    public void setOnCardClickListener(OnCardClickListener listener) {
        this.onCardClickListener = listener;
        setOnClickListener(v -> {
            if (onCardClickListener != null) {
                onCardClickListener.onCardClick(creditScore, CreditLevelUtil.getLevel(creditScore));
            }
        });
    }
}
