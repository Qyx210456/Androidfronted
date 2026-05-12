package com.example.androidfronted.utils

import android.content.Context
import android.util.Log
import io.noties.markwon.Markwon
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.image.glide.GlideImagesPlugin

object MarkwonProvider {
    
    private const val TAG = "MarkwonProvider"
    
    lateinit var markwon: Markwon
        private set

    fun init(context: Context) {
        Log.d(TAG, "初始化Markwon...")
        markwon = Markwon.builder(context)
            .usePlugin(TablePlugin.create(context))
            .usePlugin(TaskListPlugin.create(context))
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(GlideImagesPlugin.create(context))
            .usePlugin(object : io.noties.markwon.AbstractMarkwonPlugin() {
                override fun configureTheme(builder: MarkwonTheme.Builder) {
                    builder
                        .headingTextSizeMultipliers(floatArrayOf(1.5f, 1.4f, 1.3f, 1.2f, 1.1f, 1.0f))
                        .isLinkUnderlined(true)
                        .bulletWidth(8)
                        .headingBreakHeight(0)
                        .thematicBreakHeight(1)
                }
            })
            .build()
        Log.d(TAG, "Markwon初始化完成")
    }
}
