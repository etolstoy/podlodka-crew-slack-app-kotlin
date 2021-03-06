package ru.katella.podlodkacrewslackapp.controllers

import kotlinx.html.*
import kotlinx.html.stream.createHTML
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import ru.katella.podlodkacrewslackapp.data.repositories.UserRepository

@RestController
@RequestMapping("/tilda")
class TildaCabinetController {

    @Autowired
    private lateinit var userRepository: UserRepository

    @GetMapping
    @ResponseBody
    fun getMainPage(): String {

        val users = userRepository.findAll()
        return createHTML()
            .table {

                thead {
                    tr {
                        td { +"User id" }
                        td { +"User name" }
                        td { +"Is admin" }
                        td { +"Points" }
                    }
                }
                for (user in users) {
                    tr {
                        td { +user.id.take(3) }
                        td { +user.displayName.take(3) }
                        td {
                            input {
                                type = InputType.checkBox
                                checked = user.isAdmin
                            }
                        }
                        td { +user.points.toString() }
                    }
                }
            }
    }

}