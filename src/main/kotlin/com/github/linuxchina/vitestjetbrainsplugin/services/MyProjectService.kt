package com.github.linuxchina.vitestjetbrainsplugin.services

import com.intellij.openapi.project.Project
import com.github.linuxchina.vitestjetbrainsplugin.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
