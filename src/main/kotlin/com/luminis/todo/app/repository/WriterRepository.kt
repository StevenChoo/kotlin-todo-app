package com.luminis.todo.app.repository

import com.luminis.todo.app.entity.Writer
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface WriterRepository : JpaRepository<Writer, Long> {

    fun findByFirstName(firstName: String): Optional<Writer>
}
