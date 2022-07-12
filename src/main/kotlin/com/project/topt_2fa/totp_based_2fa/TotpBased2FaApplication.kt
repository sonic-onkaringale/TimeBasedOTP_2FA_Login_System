package com.project.topt_2fa.totp_based_2fa

import com.google.zxing.BarcodeFormat
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter
import dev.turingcomplete.kotlinonetimepassword.GoogleAuthenticator
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.http.converter.BufferedImageHttpMessageConverter
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.awt.image.BufferedImage
import java.util.*

@EnableScheduling
@SpringBootApplication
class TotpBased2FaApplication{
    companion object {
        private val log = LoggerFactory.getLogger(SpringBootApplication::class.java)
    }

    final val secret = GoogleAuthenticator.createRandomSecret()

    init {
        log.info(secret.toString())
        ping()

    }

    @Scheduled(fixedRate = 1000L)
    fun ping()
    {
        val timestamp = Date(System.currentTimeMillis())
        val code = GoogleAuthenticator(secret).generate(timestamp)
//        log.info(code)
    }

    @Bean
    fun qrwriter() = QRCodeWriter()

    @Bean
    fun imageConv():HttpMessageConverter<BufferedImage>
    {
        return BufferedImageHttpMessageConverter()
    }
}
@Component
class generator(private val writer:QRCodeWriter)
{
    fun generate(issuer:String,email:String,secret:String):BufferedImage
    {
        val uri = "otpauth://totp/${issuer}:${email}?secret=${secret}&issuer=${issuer}"
        val mat = writer.encode(uri,BarcodeFormat.QR_CODE,200,200)
        return  MatrixToImageWriter.toBufferedImage((mat))
    }
}

@RestController
class qrcodecontroller(private val gen:generator)
{
    @GetMapping("/qrcode/{secret}", produces = [org.springframework.http.MediaType.IMAGE_PNG_VALUE])
    fun qrcode(@PathVariable secret: String):BufferedImage
    {
        return gen.generate("Spring Boot ","onkar@test.com",secret)
    }
}

fun main(args: Array<String>) {
    runApplication<TotpBased2FaApplication>(*args)
}
