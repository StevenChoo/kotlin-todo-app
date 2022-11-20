package com.luminis.todo.app.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity(name = "WRITER")
data class Writer(

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "writer_seq")
    val id: Long? = null,

    @Column(name = "FIRST_NAME")
    val firstName: String?,

    @Column(name = "LAST_NAME")
    val lastName: String?
)
