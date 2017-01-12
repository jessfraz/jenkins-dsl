listView('Maintenance') {
    description('All maintenance jobs.')
    filterBuildQueue()
    filterExecutors()
    jobs {
        regex(/maintenance.*/)
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
