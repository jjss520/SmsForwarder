package com.idormy.sms.forwarder.server.controller

import com.idormy.sms.forwarder.server.model.BaseRequest
import com.idormy.sms.forwarder.server.model.EmptyData
import com.yanzhenjie.andserver.annotation.*
import java.net.Inet6Address
import java.net.NetworkInterface
import java.util.Collections

@RestController
@RequestMapping(path = ["/ipv6"])
class Ipv6Controller {

    // 远程查IPv6
    // 模仿 BatteryController 的写法，使用 BaseRequest 接收请求
    @CrossOrigin(methods = [RequestMethod.POST])
    @PostMapping("/query")
    fun query(@RequestBody bean: BaseRequest<EmptyData>): Map<String, String> {
        // 直接返回 Map，AndServer 会自动把它变成 JSON 格式的 "data" 部分
        return mapOf("ipv6" to getIPv6Address())
    }

    /**
     * 获取本机 IPv6 地址的工具方法
     */
    private fun getIPv6Address(): String {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                val addrs = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    // 过滤掉回环地址(::1)和链路本地地址(fe80开头)
                    if (!addr.isLoopbackAddress && addr is Inet6Address && !addr.isLinkLocalAddress) {
                        val hostAddress = addr.hostAddress
                        if (!hostAddress.isNullOrEmpty()) {
                            // 移除可能存在的 Scope ID (例如 %wlan0)
                            val index = hostAddress.indexOf('%')
                            return if (index > 0) hostAddress.substring(0, index) else hostAddress
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "未获取到IPv6地址"
    }
}