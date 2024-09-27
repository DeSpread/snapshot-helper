package com.despread.snapshothelper.controller

import com.despread.snapshothelper.model.SnapshotDto
import com.despread.snapshothelper.service.AptosSnapshotService
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
class SnapshotController(private val aptosSnapshotService: AptosSnapshotService) : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    @PostMapping("/snapshot/multipart")
    suspend fun snapshot(@RequestBody snapshotDto: SnapshotDto): ResponseEntity<String> {
        launch {
            aptosSnapshotService.snapshot(snapshotDto)
        }
        return ResponseEntity.accepted().body("File uploaded successfully")
    }
}