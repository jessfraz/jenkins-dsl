listView('Unstable Builds') {
    description('All failed and unstable builds.')
    filterBuildQueue()
    filterExecutors()
    jobFilters {
        status {
            matchType(MatchType.INCLUDE_UNMATCHED)
            status(Status.STABLE)
        }
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
