freeStyleJob('mirror_what_can_i_say_im_a_hopeless_open_source_romantic') {
    displayName('mirror-what-can-i-say-im-a-hopeless-open-source-romantic')
    description('Mirror github.com/jessfraz/what-can-i-say-im-a-hopeless-open-source-romantic to g.j3ss.co/what-can-i-say-im-a-hopeless-open-source-romantic.')
    checkoutRetryCount(3)
    properties {
        githubProjectUrl('https://github.com/jessfraz/what-can-i-say-im-a-hopeless-open-source-romantic')
        sidebarLinks {
            link('https://git.j3ss.co/what-can-i-say-im-a-hopeless-open-source-romantic', 'git.j3ss.co/what-can-i-say-im-a-hopeless-open-source-romantic', 'notepad.png')
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
        shell('git clone --mirror https://github.com/jessfraz/what-can-i-say-im-a-hopeless-open-source-romantic.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/what-can-i-say-im-a-hopeless-open-source-romantic.git')
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
