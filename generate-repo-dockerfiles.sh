#!/bin/bash

# This script generates the DSLs for building Dockerfiles in each of my repos.

set -e
set -o pipefail

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}"  )" && pwd  )"

generate_dsl(){
	local orig=$1
	local user=${orig%/*}
	local name=${orig#*/}

	case "${user}/${name}" in
		"kylemanna/docker-openvpn")
			name=openvpn-server
			;;
		*) ;;
	esac

	local image=${name}
	case "$image" in
		"contained.af")
			image="contained";;
		"present.j3ss.co")
			image="present";;
	esac

	rname=${name//-/_}
	rname=${rname//1/one}
	file="${DIR}/projects/repo-dockerfiles/${rname//./_}.groovy"

	echo "${file} | ${image}"

	cat <<-EOF > "$file"
freeStyleJob('${rname//./_}') {
    displayName('${name}')
    description('Build Dockerfiles in ${orig}.')

    concurrentBuild()
    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/${orig}')
        sidebarLinks {
            link('https://hub.docker.com/r/jess/${image}', 'Docker Hub: jess/${image}', 'notepad.png')
            link('https://hub.docker.com/r/jessfraz/${image}', 'Docker Hub: jessfraz/${image}', 'notepad.png')
            link('https://r.j3ss.co/repo/${image}/tags', 'Registry: r.j3ss.co/${image}', 'notepad.png')
EOF

	if [[ "$image" == "contained" ]]; then
		cat <<-EOF >> "$file"
            link('https://hub.docker.com/r/jess/docker', 'Docker Hub: jess/docker', 'notepad.png')
            link('https://hub.docker.com/r/jessfraz/docker', 'Docker Hub: jessfraz/docker', 'notepad.png')
            link('https://r.j3ss.co/repo/docker/tags', 'Registry: r.j3ss.co/docker', 'notepad.png')
		EOF
	fi

	cat <<-EOF >> "$file"
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
            branches('*/master', '*/tags/*')
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
        shell('docker build --rm --force-rm -t r.j3ss.co/${image}:latest .')
        shell('docker tag r.j3ss.co/${image}:latest jess/${image}:latest')
        shell('docker tag r.j3ss.co/${image}:latest jessfraz/${image}:latest')
        shell('docker push --disable-content-trust=false r.j3ss.co/${image}:latest')
        shell('docker push --disable-content-trust=false jess/${image}:latest')
        shell('docker push --disable-content-trust=false jessfraz/${image}:latest')
        shell('for tag in \$(git tag); do git checkout \$tag; docker build  --rm --force-rm -t r.j3ss.co/${image}:\$tag . || true; docker push --disable-content-trust=false r.j3ss.co/${image}:\$tag || true; docker tag r.j3ss.co/${image}:\$tag jess/${image}:\$tag || true; docker push --disable-content-trust=false jess/${image}:\$tag || true; done')
		EOF

	if [[ "$image" == "contained" ]]; then
		cat <<-EOF >> "$file"
        shell('git checkout master')
        shell('docker build --rm --force-rm -f Dockerfile.dind -t r.j3ss.co/docker:userns .')
        shell('docker tag r.j3ss.co/docker:userns jess/docker:userns')
        shell('docker tag r.j3ss.co/docker:userns jessfraz/docker:userns')
        shell('docker push --disable-content-trust=false r.j3ss.co/docker:userns')
	    shell('docker push --disable-content-trust=false jess/docker:userns')
	    shell('docker push --disable-content-trust=false jessfraz/docker:userns')
		EOF
	fi

	cat <<-EOF >> "$file"
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
genuinetools/1up
genuinetools/amicontained
genuinetools/apk-file
genuinetools/audit
genuinetools/bane
genuinetools/bpfd
genuinetools/bpfps
genuinetools/certok
genuinetools/contained.af
genuinetools/ghb0t
genuinetools/img
genuinetools/magneto
genuinetools/netns
genuinetools/pepper
genuinetools/reg
genuinetools/releases
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
jessfraz/morningpaper2remarkable
jessfraz/netscan
jessfraz/party-clippy
jessfraz/pastebinit
jessfraz/pony
jessfraz/present.j3ss.co
jessfraz/s3server
jessfraz/secping
jessfraz/ship
jessfraz/snippetlib
jessfraz/tdash
jessfraz/tripitcalb0t
kylemanna/docker-openvpn
)

main(){
	rm -rf "$DIR/projects/repo-dockerfiles"
	mkdir -p "$DIR/projects/repo-dockerfiles"

	echo "FILE | IMAGE"

	for r in "${repos[@]}"; do
		generate_dsl "${r}"
	done
}

main
