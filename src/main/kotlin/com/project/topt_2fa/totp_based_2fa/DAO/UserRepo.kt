package com.project.topt_2fa.totp_based_2fa.DAO

import com.project.topt_2fa.totp_based_2fa.Model.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepo : JpaRepository<User, Int> {
    fun findByEmail(email: String): User

    fun countByEmailEquals(email: String): Int

    fun findByEmailAndPass(email: String, pass: String): List<User>
}

