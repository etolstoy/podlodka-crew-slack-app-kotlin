package ru.katella.podlodkacrewslackapp.controllers

import com.slack.api.bolt.App
import com.slack.api.bolt.servlet.SlackAppServlet
import javax.servlet.annotation.WebServlet

@WebServlet("/slack/events")
class SlackAppController(app: App): SlackAppServlet(app)