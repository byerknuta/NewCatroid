package org.catrobat.catroid.content

import android.util.Log
import kotlinx.coroutines.*
import org.catrobat.catroid.utils.ErrorLog
import java.io.*
import java.net.*

class LocalServer private constructor() {
    companion object {
        private var serverSocket: ServerSocket? = null
        private var clientSocket: Socket? = null
        private var outputStream: OutputStream? = null

        @Volatile private var connectedPort: String? = null
        @Volatile private var connectedIP: String? = null
        @Volatile private var receivedValue: String = ""
        @Volatile private var isRunning = false

        private var job: Job? = null
        private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

        fun startOrJoin(ip: String?, port: String) {
            stop()

            job = coroutineScope.launch {
                try {
                    isRunning = true
                    if (ip.isNullOrEmpty()) {
                        startServer(port)
                    } else {
                        connectToServer(ip, port)
                    }
                } catch (e: Exception) {
                    if (isRunning) {
                        ErrorLog.log(e.message ?: "Unknown socket error")
                        Log.e("LocalServer", "Ошибка: ${e.message}", e)
                    }
                } finally {
                    stop()
                }
            }
        }

        private suspend fun startServer(port: String) = withContext(Dispatchers.IO) {
            serverSocket = ServerSocket(port.toInt())
            connectedPort = port
            connectedIP = getLocalIPAddress()
            Log.d("LocalServer", "Сервер запущен на $connectedIP:$port")

            val socket = serverSocket!!.accept()
            setupConnection(socket)
        }

        private suspend fun connectToServer(ip: String, port: String) = withContext(Dispatchers.IO) {
            val socket = Socket(ip, port.toInt())
            connectedIP = ip
            connectedPort = port
            setupConnection(socket)
        }

        private fun setupConnection(socket: Socket) {
            clientSocket = socket
            outputStream = socket.getOutputStream()
            listenForMessages(socket)
        }

        private fun listenForMessages(socket: Socket) {
            try {
                BufferedReader(InputStreamReader(socket.getInputStream())).use { reader ->
                    while (isRunning && !socket.isClosed) {
                        val message = reader.readLine()
                        if (message == null) {
                            Log.d("LocalServer", "Соединение разорвано удаленной стороной.")
                            break
                        }
                        receivedValue = message
                    }
                }
            } catch (e: Exception) {
                if (isRunning) Log.e("LocalServer", "Ошибка чтения: ${e.message}")
            } finally {
                stop()
            }
        }

        fun send(value: String) {
            coroutineScope.launch {
                val out = outputStream
                if (out == null) {
                    Log.e("LocalServer", "Соединение не установлено.")
                    return@launch
                }
                try {
                    out.write((value + "\n").toByteArray(Charsets.UTF_8))
                    out.flush()
                } catch (e: Exception) {
                    Log.e("LocalServer", "Ошибка отправки: ${e.message}")
                    stop()
                }
            }
        }

        @Synchronized
        fun stop() {
            if (!isRunning) return
            isRunning = false
            try {
                outputStream?.close()
                clientSocket?.close()
                serverSocket?.close()
            } catch (e: IOException) {
                Log.w("LocalServer", "Ошибка при закрытии: ${e.message}")
            } finally {
                outputStream = null
                clientSocket = null
                serverSocket = null
                job?.cancel()
                job = null
            }
        }

        fun getValue(): String = receivedValue
        fun getIP(): String = connectedIP ?: "NaN"
        fun getPort(): String = connectedPort ?: "NaN"

        private fun getLocalIPAddress(): String? {
            return try {
                NetworkInterface.getNetworkInterfaces().toList()
                    .flatMap { it.inetAddresses.toList() }
                    .firstOrNull { !it.isLoopbackAddress && it is Inet4Address }
                    ?.hostAddress
            } catch (e: Exception) {
                null
            }
        }
    }
}
