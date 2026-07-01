package com.example.netmusicandroid

import com.example.netmusicandroid.data.repository.RepositoryErrorParser
import org.junit.Assert.assertEquals
import org.junit.Test

class RepositoryErrorParserTest {
    @Test
    fun parse_returnsThrowableMessageWhenPresent() {
        val message = RepositoryErrorParser.parse(IllegalStateException("接口异常"))

        assertEquals("接口异常", message)
    }

    @Test
    fun parse_returnsDefaultNetworkMessageWhenThrowableHasNoMessage() {
        val message = RepositoryErrorParser.parse(Throwable())

        assertEquals("网络异常", message)
    }
}
