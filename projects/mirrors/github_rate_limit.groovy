freeStyleJob('mirror_github_rate_limit') {
    displayName('mirror-github-rate-limit')
    description('Mirror github.com/jessfraz/github-rate-limit to g.j3ss.co/github-rate-limit.')
    checkoutRetryCount(3)
    properties {
        githubProjectUrl('https://github.com/jessfraz/github-rate-limit')
        sidebarLinks {
            link('https://git.j3ss.co/github-rate-limit', 'git.j3ss.co/github-rate-limit', 'notepad.png')
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
        shell('git clone --mirror https://github.com/jessfraz/github-rate-limit.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/github-rate-limit.git')
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
