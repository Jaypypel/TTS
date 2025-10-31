package com.example.neptune.ttsapp.Util

object js {
    var text_input_regex= Regex(pattern = """^[\p{L}\p{N}\s.,|?'"()\-]{1,200}$""",
        options =setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE)
    )
}