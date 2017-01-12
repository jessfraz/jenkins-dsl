listView('Update Forks') {
    description('All jobs that update a forked repository.')
    filterBuildQueue()
    filterExecutors()
    jobs {
        regex(/update_fork.*/)
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
