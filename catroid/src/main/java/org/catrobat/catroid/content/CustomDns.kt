import android.util.Log
import java.net.InetAddress
import java.net.UnknownHostException
import okhttp3.Dns
import org.catrobat.catroid.content.GeminiManager

class CustomDns : Dns {
    override fun lookup(hostname: String): List<InetAddress> {
        return try {
            if (GeminiManager.dns_server.isNullOrEmpty()) {
                Dns.SYSTEM.lookup(hostname)
            } else {
                Log.d("CustomDns", "Resolving hostname using DNS server: ${GeminiManager.dns_server}")
                val addresses = InetAddress.getAllByName(GeminiManager.dns_server)
                addresses.toList()
            }
        } catch (e: UnknownHostException) {
            Log.e("CustomDns", "Error resolving DNS: ${e.message}")
            throw e
        }
    }
}
