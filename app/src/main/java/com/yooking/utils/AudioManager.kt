package com.yooking.utils

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.fragment.app.FragmentActivity
import com.permissionx.guolindev.PermissionX
import com.yooking.ffmpegdemo.AudioService
import com.yooking.utils.ext.yes
import com.yooking.ffmpegdemo.MainActivity
import androidx.core.content.ContextCompat.startForegroundService
import android.os.Build




/**
 * 收款播报广播工具
 * Created by yooking on 2020/8/13.
 * Copyright (c) 2020 yooking. All rights reserved.
 */
object AudioManager {

    const val PATH_CONCAT_AUDIO = "/concatAudio.mp3"

    const val KEY_MESSAGE = "message"
    private const val KEY_ACTION = "audioReceiver"

    fun sendBroadcast(context: Context, message: String) {
        val intent = Intent()
        intent.action = KEY_ACTION
        intent.putExtra(KEY_MESSAGE, message)
        context.sendBroadcast(intent)
    }

    private fun startAudioService(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //android8.0以上通过startForegroundService启动service
            context.startForegroundService(Intent(context, AudioService().javaClass))
        } else {
            context.startService(Intent(context, AudioService().javaClass))
        }
    }

    fun checkPermissionAndStartService(context: FragmentActivity) {
        val permissionMap = mapOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE to "文件写入权限",
            Manifest.permission.READ_EXTERNAL_STORAGE to "文件读取权限"
        )
        //1 权限判断
        PermissionX.init(context)
            .permissions(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            .onExplainRequestReason { scope, deniedList ->
                var permissionNames = ""
                for (perName in deniedList) {
                    permissionNames += "\n" + permissionMap[perName]
                }
                scope.showRequestReasonDialog(
                    deniedList,
                    "应用需要以下:${permissionNames}\n用于初始化语音播报功能",
                    "确定"
                )
            }
            .onForwardToSettings { scope, deniedList ->
                var permissionNames = ""
                for (perName in deniedList) {
                    permissionNames += "\n" + permissionMap[perName]
                }
                scope.showForwardToSettingsDialog(
                    deniedList,
                    "应用需要以下:${permissionNames}\n用于初始化语音播报功能，请到设置中授权",
                    "确定"
                )
            }
            .request { allGranted, _, _ ->
                allGranted.yes {
                    //启动服务
                    startAudioService(context)
                }
            }
    }

    fun registerBroadcast(context: Context, receiver: BroadcastReceiver) {
        //注册广播接收器
        val filter = IntentFilter()
        filter.addAction(KEY_ACTION)
        context.registerReceiver(receiver, filter)
    }
}