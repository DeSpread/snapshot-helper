package com.despread.snapshothelper.controller

import com.despread.snapshothelper.exntends.asCoroutineContext
import com.despread.snapshothelper.model.SnapshotDto
import com.despread.snapshothelper.service.SnapshotService
import io.micrometer.tracing.Tracer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import kotlin.coroutines.CoroutineContext


@RestController
@RequestMapping("/api/v1")
class SnapshotController(private val tracer: Tracer, private val snapshotService: SnapshotService) :
    CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    @PostMapping("/snapshot/multipart")
    suspend fun snapshot(@RequestBody snapshotDto: SnapshotDto): ResponseEntity<String> {
        val span = tracer.nextSpan().name("snapshotMultipart").start()
        launch(Dispatchers.IO + span.asCoroutineContext()) {
            snapshotService.snapshot(snapshotDto)
        }
        return ResponseEntity.accepted().body("File uploaded successfully")
    }
}