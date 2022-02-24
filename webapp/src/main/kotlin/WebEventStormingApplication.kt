package com.youramaryllis.eventstorming.web

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication(scanBasePackages = ["com.bina.tools.es.web"])
class WebEventStormingApplication

fun main(args: Array<String>) {
    SpringApplication.run(WebEventStormingApplication::class.java, *args)
}




