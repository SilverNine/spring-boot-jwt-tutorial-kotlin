package me.silvernine.tutorial

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class JwtTutorialApplication

fun main(args: Array<String>) {
    runApplication<JwtTutorialApplication>(*args)
}
