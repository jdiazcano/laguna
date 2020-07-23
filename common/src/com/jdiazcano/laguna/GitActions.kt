package com.jdiazcano.laguna

import com.jdiazcano.laguna.files.File
import com.jdiazcano.laguna.git.GitRepository
import com.jdiazcano.laguna.misc.debug

internal fun prepareRepository(repoFolder: File) {
    val repo = GitRepository(repoFolder)
    debug("Opening repository")
    repo.open()
    debug("Cleaning repository")
    repo.clean()
    debug("Fetching repository")
    repo.fetch()
    debug("Checking out master")
    repo.checkout("master")
    debug("Pulling repository")
    repo.pull()
    debug("Closing repository")
    repo.close()
}