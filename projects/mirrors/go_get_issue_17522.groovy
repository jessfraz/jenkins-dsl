freeStyleJob('mirror_go_get_issue_17522') {
    displayName('mirror-go-get-issue-17522')
    description('Mirror github.com/jessfraz/go-get-issue-17522 to g.j3ss.co/go-get-issue-17522.')
    checkoutRetryCount(3)
    properties {
        githubProjectUrl('https://github.com/jessfraz/go-get-issue-17522')
        sidebarLinks {
            link('https://git.j3ss.co/go-get-issue-17522', 'git.j3ss.co/go-get-issue-17522', 'notepad.png')
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
        shell('git clone --mirror https://github.com/jessfraz/go-get-issue-17522.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/go-get-issue-17522.git')
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
