package com.testingtech.car2x.hmi;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;

public class GuiUpdater extends Activity {

    private ProgressBar pBar;
    private AnimationDrawable logoAnimation;
    private Button btnStart, btnStop;
    private TextView noticeText, statusRunningText;
    private ScrollView stages;
    private TableLayout table;
    private int stageCount;

    public GuiUpdater(ProgressBar pBar, AnimationDrawable logoAnimation,
                      Button btnStart, Button btnStop, TextView noticeText,
                      TextView statusRunningText, ScrollView stages,
                      TableLayout table, int stageCount) {
        this.pBar = pBar;
        this.logoAnimation = logoAnimation;
        this.btnStart = btnStart;
        this.btnStop = btnStop;
        this.noticeText = noticeText;
        this.statusRunningText = statusRunningText;
        this.stages = stages;
        this.stageCount = stageCount;
        this.table = table;
    }

    public void resetTestRunnerGui() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                noticeText.setBackgroundColor(Color.TRANSPARENT);
                statusRunningText.setText(R.string.textview_not_running);
                for (int i = 0; i < table.getChildCount(); i++) {
                    table.getChildAt(i).setBackgroundColor(Color.TRANSPARENT);
                }
                enableStartButton(true);
                animateLogo(false);
            }
        });
    }

    public void updateProgressBar(final int stage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (stage < 0) {
                    pBar.setProgress(0);
                } else if (stage > stageCount) {
                    pBar.setProgress(100);
                } else {
                    pBar.setProgress(stage * 100 / stageCount);
                }
            }
        });
    }

    public void animateLogo(final boolean animate) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (animate) {
                    logoAnimation.start();
                } else {
                    logoAnimation.stop();
                }
            }
        });
    }

    public void setStatusText(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                statusRunningText.setText(text);
            }
        });
    }

    public void setNoticeText(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                noticeText.setText(text);
            }
        });
    }

    public void scrollToStage(final int stage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Logger.writeLog("GUIUPDATER: scroll to stage " + stage);
                if (stage < 0 || stage > stageCount)
                    return;
                stages.smoothScrollTo(0, stage);
                if (stage > 0) {
                    table.getChildAt(stage - 1).setBackgroundColor(Color.TRANSPARENT);
                }
                table.getChildAt(stage).setBackgroundResource(R.drawable.rectangle_border_red);
            }
        });
    }

    public void enableStartButton(final boolean enable) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (enable) {
                    btnStart.setEnabled(true);
                    btnStop.setEnabled(false);
                } else {
                    btnStart.setEnabled(false);
                    btnStop.setEnabled(true);
                }
            }
        });
    }
}
