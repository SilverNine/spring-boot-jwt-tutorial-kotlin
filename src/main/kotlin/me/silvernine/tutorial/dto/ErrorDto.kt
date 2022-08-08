package me.silvernine.tutorial.dto

import org.springframework.validation.FieldError

class ErrorDto(val status: Int, val message: String) {
    private val fieldErrors: MutableList<FieldError> = ArrayList()
    fun addFieldError(objectName: String?, path: String?, message: String?) {
        val error = FieldError(objectName!!, path!!, message!!)
        fieldErrors.add(error)
    }

    fun getFieldErrors(): List<FieldError> {
        return fieldErrors
    }
}