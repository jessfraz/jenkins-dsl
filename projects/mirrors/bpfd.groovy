freeStyleJob('mirror_bpfd') {
    displayName('mirror-bpfd')
    description('Mirror github.com/genuinetools/bpfd to g.j3ss.co/genuinetools/bpfd.')
    checkoutRetryCount(3)
    properties {
        githubProjectUrl('https://github.com/genuinetools/bpfd')
        sidebarLinks {
            link('https://git.j3ss.co/genuinetools/bpfd', 'git.j3ss.co/genuinetools/bpfd', 'notepad.png')
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
        shell('git clone --mirror https://github.com/genuinetools/bpfd.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/genuinetools/bpfd.git')
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
