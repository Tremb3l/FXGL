/*
 * FXGL - JavaFX Game Library. The MIT License (MIT).
 * Copyright (c) AlmasB (almaslvl@gmail.com).
 * See LICENSE for details.
 */
@file:Suppress("JAVA_MODULE_DOES_NOT_DEPEND_ON_MODULE")
package com.almasb.fxgl.logging

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.nio.file.Files
import java.nio.file.Paths

/**
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
class LoggerTest {

    companion object {
        @BeforeAll
        @JvmStatic fun before() {
            cleanUp()
        }

        @AfterAll
        @JvmStatic fun cleanUp() {
            Paths.get("testDir").toFile().deleteRecursively()

            assertTrue(!Files.exists(Paths.get("testDir")), "test dir is present before")
        }
    }

    @Test
    fun `Configuring logger more than once does not throw`() {
        Logger.configure(LoggerConfig())

        assertDoesNotThrow {
            Logger.configure(LoggerConfig())
        }
    }

    @Test
    fun `Closing logger more than once does not throw`() {
        Logger.close()

        assertDoesNotThrow {
            Logger.close()
        }
    }

    @Test
    fun `Logger levels`() {
        val log = Logger.get("test")

        val output1 = object : LoggerOutput {
            val testMessages = arrayListOf<String>()

            override fun append(message: String) {
                testMessages += message
            }

            override fun close() {
            }
        }

        val output2 = object : LoggerOutput {
            val testMessages = arrayListOf<String>()

            override fun append(message: String) {
                testMessages += message
            }

            override fun close() {
            }
        }

        Logger.addOutput(output1, LoggerLevel.INFO)
        Logger.addOutput(output2, LoggerLevel.FATAL)

        log.warning("hello")

        assertThat(output1.testMessages.size, `is`(1))
        assertThat(output2.testMessages.size, `is`(0))

        log.info("world")

        assertThat(output1.testMessages.size, `is`(2))
        assertThat(output2.testMessages.size, `is`(0))

        log.fatal("bye!")

        assertThat(output1.testMessages.size, `is`(3))
        assertThat(output2.testMessages.size, `is`(1))

        Logger.removeOutput(output1, LoggerLevel.INFO)
        Logger.removeOutput(output2, LoggerLevel.FATAL)
    }

    @Test
    fun `Default message formatting`() {
        val formatter = DefaultMessageFormatter()
        var message = formatter.makeMessage("DateTime", "ThreadName", "LoggerLevel", "SuperLongLoggerNameToBeTruncated", "LoggerMessage")

        assertThat(message, `is`("DateTime [ThreadName               ] Logge SuperLongLoggerNameT - LoggerMessage"))

        message = formatter.makeMessage("DateTime", "ThreadName", "LoggerLevel", "ShortName", "LoggerMessage")

        assertThat(message, `is`("DateTime [ThreadName               ] Logge ShortName            - LoggerMessage"))
    }

    @Test
    fun `Test ConsoleOutput`() {
        val output = ConsoleOutput()

        output.append("Hello Test World")

        // does nothing
        output.close()
    }

    @Test
    fun `Test FileOutput`() {
        val logDir = Paths.get("testDir")

        assertFalse(Files.exists(logDir))

        val output = FileOutput("test", "testDir/", 5)

        assertTrue(Files.exists(logDir))

        output.append("Hello Test World")
        output.append("Hello Test World 2")

        // writes to FS
        output.close()

        val logFile = Files.walk(logDir)
                .filter { it.toAbsolutePath().toString().endsWith("log") }
                .findAny()

        assertTrue(logFile.isPresent)

        val lines = Files.readAllLines(logFile.get())

        assertThat(lines.size, `is`(2))
        assertThat(lines[0], `is`("Hello Test World"))
        assertThat(lines[1], `is`("Hello Test World 2"))
    }
}