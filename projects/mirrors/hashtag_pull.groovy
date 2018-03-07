freeStyleJob('mirror_hashtag_pull') {
    displayName('mirror-hashtag-pull')
    description('Mirror github.com/jessfraz/hashtag-pull to g.j3ss.co/hashtag-pull.')
    checkoutRetryCount(3)
    properties {
        githubProjectUrl('https://github.com/jessfraz/hashtag-pull')
        sidebarLinks {
            link('https://git.j3ss.co/hashtag-pull', 'git.j3ss.co/hashtag-pull', 'notepad.png')
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
        shell('git clone --mirror https://github.com/jessfraz/hashtag-pull.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/hashtag-pull.git')
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
