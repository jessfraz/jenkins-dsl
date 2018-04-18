freeStyleJob('mirror_cni_benchmarks') {
    displayName('mirror-cni-benchmarks')
    description('Mirror github.com/jessfraz/cni-benchmarks to g.j3ss.co/cni-benchmarks.')
    checkoutRetryCount(3)
    properties {
        githubProjectUrl('https://github.com/jessfraz/cni-benchmarks')
        sidebarLinks {
            link('https://git.j3ss.co/cni-benchmarks', 'git.j3ss.co/cni-benchmarks', 'notepad.png')
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
        shell('git clone --mirror https://github.com/jessfraz/cni-benchmarks.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/cni-benchmarks.git')
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
