package org.chorus_oss.chorus.lang

import org.chorus_oss.chorus.GameMockExtension
import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.plugin.PluginBase
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

@ExtendWith(GameMockExtension::class)
class LangTest {
    @Test
    fun test_tr() {
        Assertions.assertEquals("hhdaosidhja", i18n!!.tr(LangCode.en_US, "test1", "hhdaosidhja"))
        Assertions.assertEquals("hello \n world", i18n!!.tr(LangCode.en_US, "test2"))
        Assertions.assertEquals("你好 \n 世界", i18n!!.tr(LangCode.zh_CN, "test2"))
        Assertions.assertEquals("Hello \n World", Server.instance.lang.tr("Hello \n World"))
    }

    @Test
    fun test_placeholders() {
        Assertions.assertEquals("Test placeholders 1 2", i18n!!.tr(LangCode.en_US, "test3", "1", "2"))
        Assertions.assertEquals("测试 placeholders 1 2", i18n!!.tr(LangCode.zh_CN, "test3", "1", "2"))
        Assertions.assertEquals("§7CoolLoong §ejoin the server!", i18n!!.tr(LangCode.zh_CN, "test4", "CoolLoong"))
    }

    @Test
    fun test_addLang() {
        i18n!!.addLang(LangCode.zh_TW, "src/test/resources/language_extra/zh_TW.json")
        Assertions.assertEquals("hhdaosidhja", i18n!!.tr(LangCode.zh_TW, "test1", "hhdaosidhja"))
        Assertions.assertEquals("你好，世界！", i18n!!.tr(LangCode.zh_TW, "test2"))
    }

    @Test
    fun test_reload() {
        try {
            Files.copy(Path.of("src/test/resources/en_GB.json"), Path.of("src/test/resources/language/en_GB.json"))
        } catch (ignore: IOException) {
        }
        Assertions.assertTrue(i18n!!.reloadLangAll("src/test/resources/language"))
        Assertions.assertEquals("hhdaosidhja", i18n!!.tr(LangCode.en_GB, "test1", "hhdaosidhja"))
        Assertions.assertEquals("Heeeelo World!", i18n!!.tr(LangCode.en_GB, "test2"))
    }

    @Test
    fun test_TextContainer() {
        val textContainer = TextContainer("test")
        Assertions.assertEquals("test", textContainer.text)
        textContainer.text = "test1"
        Assertions.assertEquals("test1", textContainer.text)
    }

    companion object {
        var i18n: PluginI18n? = null

        @BeforeAll
        fun test_LoadLang() {
            val mock = Mockito.mock(PluginBase::class.java)
            Mockito.`when`(mock.file).thenReturn(File("test"))
            i18n = PluginI18nManager.register(mock, "src/test/resources/language")
        }

        @AfterAll
        fun clean() {
            try {
                Files.deleteIfExists(Path.of("src/test/resources/language/en_GB.json"))
            } catch (ignore: IOException) {
            }
        }
    }
}
