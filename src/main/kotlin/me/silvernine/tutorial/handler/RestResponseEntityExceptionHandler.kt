package me.silvernine.tutorial.handler

import me.silvernine.tutorial.dto.ErrorDTO
import me.silvernine.tutorial.exception.DuplicateMemberException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class RestResponseEntityExceptionHandler : ResponseEntityExceptionHandler() {
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(value = [DuplicateMemberException::class])
    @ResponseBody
    protected fun badRequest(ex: RuntimeException, request: WebRequest?): ErrorDTO {
        return ErrorDTO(HttpStatus.CONFLICT.value(), ex.message!!)
    }
}