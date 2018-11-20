freeStyleJob('mirror_shaking_finger_action') {
    displayName('mirror-shaking-finger-action')
    description('Mirror github.com/jessfraz/shaking-finger-action to g.j3ss.co/shaking-finger-action.')
    checkoutRetryCount(3)
    properties {
        githubProjectUrl('https://github.com/jessfraz/shaking-finger-action')
        sidebarLinks {
            link('https://git.j3ss.co/shaking-finger-action', 'git.j3ss.co/shaking-finger-action', 'notepad.png')
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
        shell('git clone --mirror https://github.com/jessfraz/shaking-finger-action.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/shaking-finger-action.git')
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
