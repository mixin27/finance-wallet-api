package com.financewallet.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableJpaRepositories
@EnableScheduling
class FinanceWalletApiApplication

fun main(args: Array<String>) {
	runApplication<FinanceWalletApiApplication>(*args)
}
