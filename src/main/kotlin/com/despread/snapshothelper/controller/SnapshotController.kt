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
@RequestMapping("/v1")
class SnapshotController(private val aptosSnapshotService: AptosSnapshotService) {

    @PostMapping("/snapshot")
    fun snapshot(@RequestBody snapshotDto: SnapshotDto) : ResponseEntity<Unit> {
        val result = aptosSnapshotService.snapshot(snapshotDto)
        return ResponseEntity(result, HttpStatus.CREATED)
    }
}