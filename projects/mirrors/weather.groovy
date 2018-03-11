freeStyleJob('mirror_weather') {
    displayName('mirror-weather')
    description('Mirror github.com/genuinetools/weather to g.j3ss.co/genuinetools/weather.')
    checkoutRetryCount(3)
    properties {
        githubProjectUrl('https://github.com/genuinetools/weather')
        sidebarLinks {
            link('https://git.j3ss.co/genuinetools/weather', 'git.j3ss.co/genuinetools/weather', 'notepad.png')
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
        shell('git clone --mirror https://github.com/genuinetools/weather.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/genuinetools/weather.git')
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
