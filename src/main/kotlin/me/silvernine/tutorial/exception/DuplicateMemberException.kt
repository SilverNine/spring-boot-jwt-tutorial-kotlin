package me.silvernine.tutorial.exception

class DuplicateMemberException(
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(message, cause)
