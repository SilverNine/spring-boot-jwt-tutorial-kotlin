package me.silvernine.tutorial.exception

class NotFoundMemberException(
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(message, cause)
