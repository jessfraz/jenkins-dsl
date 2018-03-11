freeStyleJob('mirror_apk_file') {
    displayName('mirror-apk-file')
    description('Mirror github.com/genuinetools/apk-file to g.j3ss.co/genuinetools/apk-file.')
    checkoutRetryCount(3)
    properties {
        githubProjectUrl('https://github.com/genuinetools/apk-file')
        sidebarLinks {
            link('https://git.j3ss.co/genuinetools/apk-file', 'git.j3ss.co/genuinetools/apk-file', 'notepad.png')
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
        shell('git clone --mirror https://github.com/genuinetools/apk-file.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/genuinetools/apk-file.git')
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
