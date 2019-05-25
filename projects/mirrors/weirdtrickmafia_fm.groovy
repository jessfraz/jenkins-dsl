freeStyleJob('mirror_weirdtrickmafia_fm') {
    displayName('mirror-weirdtrickmafia.fm')
    description('Mirror github.com/jessfraz/weirdtrickmafia.fm to g.j3ss.co/weirdtrickmafia.fm.')
    checkoutRetryCount(3)
    properties {
        githubProjectUrl('https://github.com/jessfraz/weirdtrickmafia.fm')
        sidebarLinks {
            link('https://git.j3ss.co/weirdtrickmafia.fm', 'git.j3ss.co/weirdtrickmafia.fm', 'notepad.png')
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
        shell('git clone --mirror https://github.com/jessfraz/weirdtrickmafia.fm.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/weirdtrickmafia.fm.git')
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
