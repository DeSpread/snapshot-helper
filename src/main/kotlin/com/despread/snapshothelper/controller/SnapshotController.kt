package com.despread.snapshothelper.controller

import com.despread.snapshothelper.model.SnapshotDto
import com.despread.snapshothelper.service.AptosSnapshotService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class SnapshotController(private val aptosSnapshotService: AptosSnapshotService) {

    @PostMapping("/snapshot")
    suspend fun snapshot(@RequestBody snapshotDto: SnapshotDto): ResponseEntity<Void> {
        return try {
            aptosSnapshotService.snapshot(snapshotDto)
            ResponseEntity(HttpStatus.CREATED)
        } catch (e: Exception) {
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}