package com.programistich.twitter.utils

import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.InputStreamReader

@Service
class UnShortenerService {

    fun shortLink(link: String): String {
        var result = link
        try {
            val command = listOf("curl", "-v", link)
            val processBuilder = ProcessBuilder(command)
            processBuilder.redirectErrorStream(true)
            val process = processBuilder.start()

            val inputStream = process.inputStream
            val bufferReader = BufferedReader(InputStreamReader(inputStream))
            bufferReader.forEachLine {
                if (it.startsWith("< location: ")) {
                    result = it.replace("< location:", "").trim()
                    return@forEachLine
                }
            }
            bufferReader.close();
            process.waitFor()
            process.destroy()
            return result
        } catch (exception: Exception) {
            exception.printStackTrace()
            return result
        }
    }
}
