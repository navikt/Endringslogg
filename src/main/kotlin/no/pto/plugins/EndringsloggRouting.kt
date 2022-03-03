package no.pto.plugins

import Err
import Ok
import SanityClient
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import no.pto.*
import no.pto.env.erIDev
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URLEncoder
import java.nio.charset.Charset
import java.util.*

val logger: Logger = LoggerFactory.getLogger("no.nav.pto.endringlogg.routing")

fun Application.configureEndringsloggRouting(client: SanityClient) {
    routing {
        post("/endringslogg") {
            val (userId, appId, dataset, maxEntries) = call.receive<BrukerData>()
            val seenEntryIds = getSeenEntriesForUser(userId).map(UUID::toString).toSet()
            val seenForcedEntryIds = getSeenForcedEntriesForUser(userId).map(UUID::toString).toSet()

            val alleMeldingerQuery = "*[_type=='$appId'][0...$maxEntries]"
            val publiserteMedlingerQuery = "*[_type=='$appId'][0...$maxEntries][publisert]"

            val query = if (erIDev()) alleMeldingerQuery else publiserteMedlingerQuery
            if(erIDev()){
                logger.info("Henter ut alle endringslogger")
            }else {
                logger.info("Henter ut publiserte endringslogger")
            }
            val queryStringEncoded = URLEncoder.encode(query, Charset.forName("utf-8"))
            when (val endringslogger = client.queryEndringslogg(queryStringEncoded, dataset)) {
                is Ok -> {
                    if (endringslogger.value.result.isEmpty()) {
                        call.response.status(HttpStatusCode(204, "Data for app $appId doesn't exist."))
                    } else {
                        call.respond(endringslogger.value.result.map {
                            it.copy(
                                seen = it.id in seenEntryIds,
                                seenForced = it.id in seenForcedEntryIds,
                                forcedModal = it.modal?.forcedModal
                            )
                        })
                    }
                }
                is Err -> {
                    logger.info("Got a client request exception with error code ${endringslogger.error.response.status.value} and message ${endringslogger.error.message}")
                    call.response.status(
                        HttpStatusCode(
                            endringslogger.error.response.status.value,
                            "Received error: ${endringslogger.error.message}"
                        )
                    )
                }
            }

        }
        post("/analytics/sett-endringer") {
            val seen = call.receive<SeenStatus>()
            insertSeenEntries(seen.userId, seen.appId, seen.documentIds.map(UUID::fromString))
            call.respond(HttpStatusCode.OK)
        }

        post("/analytics/seen-forced-modal") {
            val seen = call.receive<SeenForcedStatus>()
            insertSeenForcedEntries(seen.userId, seen.documentIds.map(UUID::fromString))
            call.respond(HttpStatusCode.OK)
        }

        post("/analytics/session-duration") {
            val duration = call.receive<SessionDuration>()
            // TODO: report to prometheus
            call.respond(HttpStatusCode.OK)
        }
        patch("/analytics/modal-open") {
            val id = call.receive<DocumentId>()
            // TODO: report to prometheus
            call.respond(HttpStatusCode.OK)
        }
        patch("/analytics/link-click") {
            val id = call.receive<DocumentId>()
            // TODO: report to prometheus
            call.respond(HttpStatusCode.OK)
        }


        get("/data/seen-all") {
            call.respond(HttpStatusCode.Gone)
        }
        get("/data/seen-app") {
            call.respond(HttpStatusCode.Gone)
        }
        get("/data/seen") {
            call.respond(HttpStatusCode.Gone)
        }
        get("data/user-session-all") {
            call.respond(HttpStatusCode.Gone)
        }
        get("data/unique-user-sessions-per-day") {
            call.respond(HttpStatusCode.Gone)
        }
    }
}