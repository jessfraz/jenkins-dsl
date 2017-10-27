freeStyleJob('mirror_go_benchmarks') {
    displayName('mirror-go-benchmarks')
    description('Mirror github.com/jessfraz/go-benchmarks to g.j3ss.co/go-benchmarks.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/go-benchmarks')
        sidebarLinks {
            link('https://git.j3ss.co/go-benchmarks', 'git.j3ss.co/go-benchmarks', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(100)
        daysToKeep(15)
    }

    triggers {
        cron('H H * * *')
    }

    wrappers { colorizeOutput() }

    steps {
        shell('git clone --mirror https://github.com/jessfraz/go-benchmarks.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/go-benchmarks.git')
    }

    publishers {
        extendedEmail {
            recipientList('$DEFAULT_RECIPIENTS')
            contentType('text/plain')
            triggers {
                stillFailing {
                    attachBuildLog(true)
                }
            }
        }

        wsCleanup()
    }
}
