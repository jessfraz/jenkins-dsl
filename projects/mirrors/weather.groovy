freeStyleJob('mirror_weather') {
    displayName('mirror-weather')
    description('Mirror github.com/jessfraz/weather to g.j3ss.co/weather.')
    checkoutRetryCount(3)
    properties {
        githubProjectUrl('https://github.com/jessfraz/weather')
        sidebarLinks {
            link('https://git.j3ss.co/weather', 'git.j3ss.co/weather', 'notepad.png')
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
        shell('git clone --mirror https://github.com/jessfraz/weather.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/weather.git')
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
