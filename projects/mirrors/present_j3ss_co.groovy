freeStyleJob('mirror_present_j3ss_co') {
    displayName('mirror-present.j3ss.co')
    description('Mirror github.com/jessfraz/present.j3ss.co to g.j3ss.co/present.j3ss.co.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/present.j3ss.co')
        sidebarLinks {
            link('https://git.j3ss.co/present.j3ss.co', 'git.j3ss.co/present.j3ss.co', 'notepad.png')
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
        shell('git clone --mirror https://github.com/jessfraz/present.j3ss.co.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/present.j3ss.co.git')
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
