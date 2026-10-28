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

    @CrossOrigin(methods = [RequestMethod.POST])
    @PostMapping("/query")
    fun query(@RequestBody bean: BaseRequest<EmptyData>): Map<String, Any> {
        Log.d(TAG, bean.data.toString())
        val ipv6List = getIPv6Addresses()
        val firstIpv6 = ipv6List.firstOrNull() ?: "未获取到IPv6地址"
        return mapOf(
            "ipv6" to firstIpv6,
            "ipv6_list" to ipv6List
        )
    }

    private fun getIPv6Addresses(): List<String> {
        val result = mutableListOf<String>()
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                if (!intf.isUp || intf.isLoopback) continue

                val addrs = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (addr is Inet6Address && !addr.isLoopbackAddress && !addr.isLinkLocalAddress && !addr.isSiteLocalAddress) {
                        var hostAddress = addr.hostAddress ?: continue
                        val index = hostAddress.indexOf('%')
                        if (index > 0) {
                            hostAddress = hostAddress.substring(0, index)
                        }
                        val lowerAddr = hostAddress.lowercase()
                        if (!lowerAddr.startsWith("fc") && !lowerAddr.startsWith("fd")) {
                            result.add(hostAddress)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "getIPv6Addresses error: " + e.message)
        }
        return result
    }
}