package cn.ppps.forwarder.server.controller

import cn.ppps.forwarder.server.model.BaseRequest
import cn.ppps.forwarder.server.model.EmptyData
import cn.ppps.forwarder.utils.Log
import com.yanzhenjie.andserver.annotation.CrossOrigin
import com.yanzhenjie.andserver.annotation.PostMapping
import com.yanzhenjie.andserver.annotation.RequestBody
import com.yanzhenjie.andserver.annotation.RequestMapping
import com.yanzhenjie.andserver.annotation.RequestMethod
import com.yanzhenjie.andserver.annotation.RestController
import java.net.Inet6Address
import java.net.NetworkInterface
import java.util.Collections

@Suppress("PrivatePropertyName")
@RestController
@RequestMapping(path = ["/ipv6"])
class Ipv6Controller {

    private val TAG: String = Ipv6Controller::class.java.simpleName

    // 远程查IPv6
    @CrossOrigin(methods = [RequestMethod.POST])
    @PostMapping("/query")
    fun query(@RequestBody bean: BaseRequest<EmptyData>): Map<String, Any> {
        Log.d(TAG, bean.data.toString())
        val ipv6List = getIPv6Addresses()
        return mapOf(
            "ipv6" to (ipv6List.firstOrNull() ?: "未获取到IPv6地址"),
            "ipv6_list" to ipv6List // 同时提供完整列表，防止第一个地址不可达
        )
    }

    /**
     * 获取本机公网 IPv6 地址列表
     */
    private fun getIPv6Addresses(): List<String> {
        val result = mutableListOf<String>()
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                // 过滤未启用、回环接口
                if (!intf.isUp || intf.isLoopback) continue

                val addrs = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (addr is Inet6Address && !addr.isLoopbackAddress && !addr.isLinkLocalAddress && !addr.isSiteLocalAddress) {
                        var hostAddress = addr.hostAddress ?: continue
                        // 移除可能存在的 Scope ID (例如 %wlan0 或 %rmnet_data0)
                        val index = hostAddress.indexOf('%')
                        if (index > 0) {
                            hostAddress = hostAddress.substring(0, index)
                        }
                        // 过滤掉 Unique Local Address (fc00::/7，通常以 fc 或 fd 开头) 和 弃用的 6to4 等地址，只保留常见全局单播地址 (2xxx: 或 3xxx:)
                        val lowerAddr = hostAddress.lowercase()
                        if (!lowerAddr.startsWith("fc") && !lowerAddr.startsWith("fd")) {
                            result.add(hostAddress)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "get