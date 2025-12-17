package com.financewallet.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
@EnableJpaRepositories
class FinanceWalletApiApplication

fun main(args: Array<String>) {
	runApplication<FinanceWalletApiApplication>(*args)
}
