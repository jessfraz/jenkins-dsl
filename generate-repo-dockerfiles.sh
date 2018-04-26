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
	if [[ "${user}/${name}" == "genuinetools/weather" ]] || [[ "${user}/${name}" == "genuinetools/reg" ]] || [[ "${user}/${name}" == "jessfraz/pastebinit" ]]; then
		image=${name}-server:${suite}
		cat <<-EOF >> $file

        shell('docker build --rm --force-rm -t r.j3ss.co/${image} server')
        shell('docker tag r.j3ss.co/${image} jess/${image}')
        shell('docker tag r.j3ss.co/${image} jessfraz/${image}')
        shell('docker push --disable-content-trust=false r.j3ss.co/${image}')
        shell('docker push --disable-content-trust=false jess/${image}')
        shell('docker push --disable-content-trust=false jessfraz/${image}')
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
genuinetools/amicontained
genuinetools/apk-file
genuinetools/audit
genuinetools/bane
genuinetools/bpfps
genuinetools/certok
genuinetools/ghb0t
genuinetools/img
genuinetools/magneto
genuinetools/netns
genuinetools/pepper
genuinetools/reg
genuinetools/riddler
genuinetools/sshb0t
genuinetools/udict
genuinetools/upmail
genuinetools/weather
jessfraz/battery
jessfraz/cliaoke
jessfraz/dstats
jessfraz/dockfmt
jessfraz/gitable
jessfraz/netscan
jessfraz/onion
jessfraz/party-clippy
jessfraz/pastebinit
jessfraz/pony
jessfraz/s3server
jessfraz/snippetlib
jessfraz/tdash
jessfraz/tripitcalb0t
kennethreitz/httpbin
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
