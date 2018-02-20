#!/bin/bash

# This script generates the DSLs for building Dockerfiles in each of my repos.

set -e
set -o pipefail

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}"  )" && pwd  )"

generate_dsl(){
	local orig=$1
	local user=${orig%/*}
	local name=${orig#*/}
	local suite=latest

	case "${user}/${name}" in
		"kylemanna/docker-openvpn")
			name=openvpn-server
			;;
		"rroemhild/docker-ejabberd")
			name=ejabberd
			;;
		*) ;;
	esac

	local image=${name}:${suite}

	rname=${name//-/_}
	file="${DIR}/projects/repo-dockerfiles/${rname//./_}.groovy"

	echo "${file} | ${image}"

	cat <<-EOF > $file
freeStyleJob('${rname//./_}') {
    displayName('${name}')
    description('Build Dockerfiles in ${orig}.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/${orig}')
        sidebarLinks {
            link('https://hub.docker.com/r/jess/${name}', 'Docker Hub: jess/${name}', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(100)
        daysToKeep(15)
    }

    scm {
        git {
            remote {
                url('https://github.com/${orig}.git')
            }
			branches('*/master')
            extensions {
                wipeOutWorkspace()
                cleanAfterCheckout()
            }
        }
    }

    triggers {
        cron('H H * * *')
        githubPush()
    }

    wrappers { colorizeOutput() }

    environmentVariables(DOCKER_CONTENT_TRUST: '1')
    steps {
        shell('docker build --rm --force-rm -t r.j3ss.co/${image} .')
        shell('docker tag r.j3ss.co/${image} jess/${image}')
        shell('docker push --disable-content-trust=false r.j3ss.co/${image}')
        shell('docker push --disable-content-trust=false jess/${image}')
EOF

	# also push the weather-server & reg-server images
	if [[ "${user}/${name}" == "jessfraz/weather" ]] || [[ "${user}/${name}" == "jessfraz/reg" ]] || [[ "${user}/${name}" == "jessfraz/pastebinit" ]]; then
		image=${name}-server:${suite}
		cat <<-EOF >> $file

        shell('docker build --rm --force-rm --no-cache -t r.j3ss.co/${image} server')
        shell('docker tag r.j3ss.co/${image} jess/${image}')
        shell('docker push --disable-content-trust=false r.j3ss.co/${image}')
        shell('docker push --disable-content-trust=false jess/${image}')
EOF
	fi

	cat <<-EOF >> $file
        shell('docker rm \$(docker ps --filter status=exited -q 2>/dev/null) 2> /dev/null || true')
        shell('docker rmi \$(docker images --filter dangling=true -q 2>/dev/null) 2> /dev/null || true')
    }

    publishers {
        retryBuild {
            retryLimit(2)
            fixedDelay(15)
        }

        extendedEmail {
            recipientList('\$DEFAULT_RECIPIENTS')
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
EOF

}

repos=(
jessfraz/amicontained
jessfraz/apk-file
jessfraz/audit
jessfraz/bane
jessfraz/battery
jessfraz/certok
jessfraz/cliaoke
jessfraz/dockfmt
jessfraz/dstats
jessfraz/ghb0t
jessfraz/img
jessfraz/magneto
jessfraz/netns
jessfraz/netscan
jessfraz/onion
jessfraz/pastebinit
jessfraz/pepper
jessfraz/pony
jessfraz/reg
jessfraz/riddler
jessfraz/s3server
jessfraz/snippetlib
jessfraz/sshb0t
jessfraz/udict
jessfraz/upmail
jessfraz/weather
kylemanna/docker-openvpn
rroemhild/docker-ejabberd
)

main(){
	mkdir -p $DIR/projects/repo-dockerfiles
	echo "FILE | IMAGE"

	for r in "${repos[@]}"; do
		generate_dsl "${r}"
	done
}

main
