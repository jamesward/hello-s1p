package com.example.service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationListener
import org.springframework.context.support.beans
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Flux

@SpringBootApplication
class ServiceApplication

fun main(args: Array<String>) {
    runApplication<ServiceApplication>(*args) {
        val ctx = beans {
            bean {
                val repository = ref<ReservationRepository>()
                router {
                    GET("/reservations") {
                        ServerResponse.ok().body(repository.findAll())
                    }
                }
            }
            bean {
                val repository = ref<ReservationRepository>()
                ApplicationListener<ApplicationReadyEvent> {

                    val names = Flux.just("Matt", "James", "Josh")
                            .map { Reservation(name = it) }
                            .flatMap { repository.save(it) }

                    repository
                            .deleteAll()
                            .thenMany(names)
                            .thenMany(repository.findAll())
                            .subscribe { println(it) }
                }
            }
        }
        addInitializers(ctx)
    }
}

data class Reservation(var id: String? = null, val name: String)

interface ReservationRepository : ReactiveCrudRepository<Reservation, String>