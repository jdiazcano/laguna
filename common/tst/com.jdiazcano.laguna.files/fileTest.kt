package com.jdiazcano.laguna.files

import com.jdiazcano.laguna.misc.pwd
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FileTest {

    @Test
    fun `a file object can be created`() {
        val file = File("path")

        assertEquals("path", file.path)
    }

    @Test
    fun `absolute path`() {
        val file = File("path")

        val realpath = pwd()
        assertEquals(realpath + File.pathSeparator + "path", file.absolutePath)
    }

    @Test
    fun `resolve a file with string`() {
        val file = File("path").resolve("subpath")

        assertEquals("path" + File.pathSeparator + "subpath", file.path)
    }

    @Test
    fun `resolve a file with another file`() {
        val file = File("path").resolve(File("subpath"))

        assertEquals("path" + File.pathSeparator + "subpath", file.path)
    }

    @Test
    fun `resolve a file with another file source has separator`() {
        val file = File("path/").resolve(File("subpath"))

        assertEquals("path" + File.pathSeparator + "subpath", file.path)
    }

    @Test
    fun `resolve a file with another file resolved has separator`() {
        val file = File("path${File.pathSeparator}").resolve(File("${File.pathSeparator}subpath"))

        assertEquals("path" + File.pathSeparator + "subpath", file.path)
    }

    @Test
    fun `is directory`() {
        val file = File(".")

        assertTrue(file.isDirectory())
    }

    @Test
    fun `is file`() {
        val file = File("build.gradle.kts")

        assertFalse(file.isDirectory())
    }

    @Test
    fun `file exists`() {
        val file = File("build.gradle.kts")

        assertTrue(file.exists())
    }

    @Test
    fun `file does not exist`() {
        val file = File("randomfilethatwontexist")

        assertFalse(file.exists())
    }

    @Test
    fun `folder exists`() {
        val file = File(".")

        assertTrue(file.exists())
    }

}