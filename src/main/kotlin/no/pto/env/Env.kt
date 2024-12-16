package no.pto.env

import java.net.URLEncoder
import java.nio.charset.Charset

val DB_USERNAME: String = System.getenv("DB_USERNAME") ?: ""
val DB_PASSWORD: String = System.getenv("DB_PASSWORD") ?: ""
val DB_HOST: String = System.getenv("DB_HOST") ?: ""
val DB_DATABASE: String = System.getenv("DB_DATABASE") ?: ""
val DB_PORT: Int = System.getenv("DB_PORT")?.toInt() ?: 5432
val SANITY_PROJECT_ID: String = "li581mqu"
val API_VERSION_ENDRINGSLOGG: String = "v2021-10-21"

private val systemmeldingPoaoQ1Query = URLEncoder.encode("*[_type=='alert_overiskten'][publisert_q1]|order(_createdAt desc)", Charset.forName("utf-8"))
private val systemmeldingPoaoProdQuery = URLEncoder.encode("*[_type=='alert_overiskten'][publisert]|order(_createdAt desc)", Charset.forName("utf-8"))

private val endringsloggPoaoQ1Query = URLEncoder.encode("*[_type=='afolg']|order(_createdAt desc)", Charset.forName("utf-8"))
private val endringsloggPoaoProdQuery = URLEncoder.encode("*[_type=='afolg'][publisert]|order(_createdAt desc)", Charset.forName("utf-8"))

fun erIProd() = System.getenv("NAIS_CLUSTER_NAME") == "prod-gcp"

fun getSystemmeldingPoaoQuery(): String = if (erIProd()) systemmeldingPoaoProdQuery else systemmeldingPoaoQ1Query
fun getEndringsloggPoaoQuery(): String = if (erIProd()) endringsloggPoaoProdQuery else endringsloggPoaoQ1Query
