/*
 * Javalin - https://javalin.io
 * Copyright 2017 David Åse
 * Licensed under Apache 2.0: https://github.com/tipsy/javalin/blob/master/LICENSE
 */

package io.javalin.core.util

import io.javalin.Context
import io.javalin.staticfiles.Location
import java.net.URL

/**
 * This is just a glorified 404 handler.
 * Ex: app.enableSinglePageMode("/my-path", "index.html")
 * If no routes or static files are found on "/my-path/" (or any subpath), index.html will be returned
 */
class SinglePageHandler {

    private val pathUrlMap = mutableMapOf<String, URL>()
    private val pathPageMap = mutableMapOf<String, String>()

    fun add(path: String, filePath: String, location: Location) {
        pathUrlMap[path] = when (location) {
            Location.CLASSPATH -> Util.getResourceUrl(filePath.removePrefix("/")) ?: throw IllegalArgumentException("File at '$filePath' not found. Path should be relative to resource folder.")
            Location.EXTERNAL -> Util.getFileUrl(filePath) ?: throw IllegalArgumentException("External file at '$filePath' not found.")
        }
        pathPageMap[path] = pathUrlMap[path]!!.readText()
    }

    fun handle(ctx: Context): Boolean {
        if (!ContextUtil.acceptsHtml(ctx)) return false
        for (path in pathPageMap.keys) {
            if (ctx.path().startsWith(path)) {
                ctx.html(when (ContextUtil.isLocalhost(ctx)) {
                    true -> pathUrlMap[path]!!.readText() // is localhost, read file again
                    false -> pathPageMap[path]!! // not localhost, use cached content
                })
                return true
            }
        }
        return false
    }

}
