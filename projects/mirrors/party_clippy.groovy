freeStyleJob('mirror_party_clippy') {
    displayName('mirror-party-clippy')
    description('Mirror github.com/jessfraz/party-clippy to g.j3ss.co/party-clippy.')
    checkoutRetryCount(3)
    properties {
        githubProjectUrl('https://github.com/jessfraz/party-clippy')
        sidebarLinks {
            link('https://git.j3ss.co/party-clippy', 'git.j3ss.co/party-clippy', 'notepad.png')
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
        shell('git clone --mirror https://github.com/jessfraz/party-clippy.git repo')
        shell('cd repo && git push --mirror ssh://git@g.j3ss.co:2200/~/party-clippy.git')
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
