package com.despread.snapshothelper.exntends

import io.micrometer.tracing.Span
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

fun Span.asCoroutineContext(): CoroutineContext = TracingContext(this)

class TracingContext(val span: Span) : AbstractCoroutineContextElement(TracingContext) {
    companion object Key : CoroutineContext.Key<TracingContext>
}