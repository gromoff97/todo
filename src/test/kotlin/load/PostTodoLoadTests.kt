package load

import BaseTests
import assertk.assertThat
import assertk.assertions.isLessThan
import assertk.assertions.isZero
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.http.entity.ContentType.APPLICATION_JSON
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler
import us.abstracta.jmeter.javadsl.JmeterDsl.rpsThreadGroup
import us.abstracta.jmeter.javadsl.JmeterDsl.testPlan
import us.abstracta.jmeter.javadsl.core.TestPlanStats
import us.abstracta.jmeter.javadsl.core.preprocessors.DslJsr223PreProcessor.PreProcessorVars
import us.abstracta.jmeter.javadsl.http.DslHttpSampler
import utils.general.EnvironmentConfig.appHost
import utils.general.EnvironmentConfig.appPort
import utils.general.EnvironmentConfig.appScheme
import utils.general.JsonUtils.toJsonNodeNow
import utils.general.RandomPojoProvider.randomValidTodoCreationRequest
import utils.general.RandomStringUtils.randomString
import java.time.Duration
import java.util.concurrent.atomic.AtomicLong
import java.util.function.Function
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

class PostTodoLoadTests : BaseTests() {

    @Test
    @DisplayName("With RPS = $USUAL_RPS value of P99 is less than $DEFAULT_DEGRADATION_THRESHOLD_MS ms")
    fun `post todo load test`() {
        val threadGroup = rpsThreadGroup().maxThreads(USUAL_RPS * MAX_EXPECTED_RESPONSE_TIME)
            .rampToAndHold(USUAL_RPS.toDouble(), Duration.ZERO, 60.seconds.toJavaDuration())
            .children(postTodoSampler(appScheme, appHost, appPort))

        val stats = testPlan(threadGroup).run().also(::log)

        with(stats.overall()) {
            assertThat(sampleTime().perc99().toMillis()).isLessThan(DEFAULT_DEGRADATION_THRESHOLD_MS)
            assertThat(errorsCount()).isZero()
        }
    }

    companion object {
        private fun postTodoSampler(scheme: String, host: String, port: Int): DslHttpSampler {
            val requestId = AtomicLong(0)
            val todoCreationBodySupplier = Function<PreProcessorVars, String> {
                randomValidTodoCreationRequest(
                    id = requestId.getAndIncrement().toBigInteger(),
                    text = randomString(100),
                    completed = Random.nextBoolean()
                ).toJsonNodeNow().toString()
            }
            return httpSampler("$scheme://$host:$port/todos").post(todoCreationBodySupplier, APPLICATION_JSON)
        }

        private fun log(testPlanStats: TestPlanStats) {
            val overallSummary = testPlanStats.overall()
            val sampleTime = overallSummary.sampleTime()

            logger.info {
                """
                =============== SUMMARY ==================
                Mean response time: ${sampleTime.mean().toMillis()} ms
                Min response time: ${sampleTime.min().toMillis()} ms
                Max response time: ${sampleTime.max().toMillis()} ms
                90th percentile: ${sampleTime.perc90().toMillis()} ms
                95th percentile: ${sampleTime.perc95().toMillis()} ms
                99th percentile: ${sampleTime.perc99().toMillis()} ms
                Total requests: ${overallSummary.samples().total()}
                Errors count: ${overallSummary.errorsCount()}
            """.trimIndent()
            }
        }

        const val USUAL_RPS = 1500
        const val DEFAULT_DEGRADATION_THRESHOLD_MS = 10L
        const val MAX_EXPECTED_RESPONSE_TIME = 260

        private val logger = KotlinLogging.logger {}
    }
}