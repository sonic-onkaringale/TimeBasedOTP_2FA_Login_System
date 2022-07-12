package com.project.topt_2fa.totp_based_2fa.Model

import javax.persistence.*

@Entity
@Table(name = "users")
open class User {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    open var id: Int? = null

    @Lob
    @Column(name = "email")
    open var email: String? = null

    @Lob
    @Column(name = "pass")
    open var pass: String? = null

    @Lob
    @Column(name = "secret")
    open var secret: String? = null
}