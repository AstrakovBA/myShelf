package com.myshelf.myshelf_app.presentation.settings

sealed class Theme {
    data object Light : Theme()
    data object Dark : Theme()
    data object System : Theme()
}
