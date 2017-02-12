listView('Mirrors') {
    description('All jobs that update a git mirror.')
    filterBuildQueue()
    filterExecutors()
    jobs {
        regex(/mirror.*/)
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
