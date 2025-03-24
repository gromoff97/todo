package utils.general

import io.qameta.allure.Allure
import io.qameta.allure.Allure.ThrowableRunnable

operator fun <T> String.invoke(throwableRunnable: ThrowableRunnable<T>): T {
    return Allure.step(this, throwableRunnable)
}