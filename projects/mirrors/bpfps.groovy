freeStyleJob('mirror_bpfps') {
    displayName('mirror-bpfps')
    description('Mirror github.com/genuinetools/bpfps to g.j3ss.co/genuinetools/bpfps.')
    checkoutRetryCount(3)
    properties {
        githubProjectUrl('https://github.com/genuinetools/bpfps')
        sidebarLinks {
            link('https://git.j3ss.co/genuinetools/bpfps', 'git.j3ss.co/genuinetools/bpfps', 'notepad.png')
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
        shell('git clone --mirror https://github.com/genuinetools/bpfps.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/genuinetools/bpfps.git')
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
