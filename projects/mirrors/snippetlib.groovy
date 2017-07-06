freeStyleJob('mirror_snippetlib') {
    displayName('mirror-snippetlib')
    description('Mirror github.com/jessfraz/snippetlib to g.j3ss.co/snippetlib.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/snippetlib')
        sidebarLinks {
            link('https://git.j3ss.co/snippetlib', 'git.j3ss.co/snippetlib', 'notepad.png')
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
        shell('git clone --mirror https://github.com/jessfraz/snippetlib.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/snippetlib.git')
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
