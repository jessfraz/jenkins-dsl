listView('Projects') {
    description('All projects.')
    filterBuildQueue()
    filterExecutors()
    jobs {
        regex(/(?!update_fork.*)(?!mirror.*)(?!.*dockerfiles)(?!.*maintenance).*/)
    }
    columns {
        status()
        weather()
        name()
        lastSuccess()
        lastFailure()
        lastDuration()
        buildButton()
    }
}
