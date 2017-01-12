listView('Dockerfiles') {
    description('All projects that build multiple Dockerfiles.')
    filterBuildQueue()
    filterExecutors()
    jobs {
        regex(/(?!mirror.*).*dockerfiles/)
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
