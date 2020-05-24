package ru.katella.podlodkacrewslackapp.slack.security

import com.slack.api.app_backend.SlackSignature
import com.slack.api.bolt.middleware.Middleware
import com.slack.api.bolt.middleware.MiddlewareChain
import com.slack.api.bolt.middleware.MiddlewareOps
import com.slack.api.bolt.request.Request
import com.slack.api.bolt.response.Response

class CustomRequestVerification(private val verifiers: List<SlackSignature.Verifier>): Middleware {

    override fun apply(req: Request<*>?, resp: Response?, chain: MiddlewareChain?): Response {
        if (MiddlewareOps.isNoSlackSignatureRequest(req!!.requestType)) {
            return chain!!.next(req)
        }
        val isValid = verifiers.any { req.isValid(it) }
        return if (isValid) {
            chain!!.next(req)
        } else {
            val signature = req.headers.getFirstValue(SlackSignature.HeaderNames.X_SLACK_SIGNATURE)
            println("Invalid signature detected - $signature")
            Response.json(401, "{\"error\":\"invalid request\"}")
        }
    }
}