package com.luminis.todo.app.controller

import com.luminis.todo.app.entity.Writer
import com.luminis.todo.app.model.WriterDTO
import com.luminis.todo.app.repository.WriterRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.lang.IllegalArgumentException
import java.util.Optional

@RestController
@RequestMapping("writer")
class WriterController(
    private val writerRepository: WriterRepository
) {

    @PostMapping
    fun createPerson(@RequestBody person: Writer) {
        val entity = Writer(firstName = person.firstName, lastName = person.lastName)
        writerRepository.save(entity)
    }

    @GetMapping
    fun getPersons(): List<WriterDTO> {
        return writerRepository.findAll().map {
            WriterDTO(it.id, it.firstName, it.firstName)
        }
    }

    @GetMapping("/{id}")
    fun getPerson(@PathVariable id: Long): Optional<WriterDTO> {
        return writerRepository.findById(id).map {
            WriterDTO(it.id, it.firstName, it.firstName)
        } ?: throw IllegalArgumentException("Person not found by id: $id")
    }
}
