#!/bin/bash

# This script creates a jenkins webhook for specific repos.

set -e
set -o pipefail

if [[ -z "$GITHUB_TOKEN" ]]; then
	echo "Set the GITHUB_TOKEN env variable."
	exit 1
fi

URI=https://api.github.com
API_VERSION=v3
API_HEADER="Accept: application/vnd.github.${API_VERSION}+json"
AUTH_HEADER="Authorization: token ${GITHUB_TOKEN}"
JENKINS_HOOK_URL="${JENKINS_HOOK_URL:-https://jenkins.j3ss.co/github-webhook/}"

create_hook(){
	repo=$1
	repo="${repo%%/hooks*}"

	echo "Creating jenkins webhook for $repo"

	curl -i -sSL -XPOST -H "${AUTH_HEADER}" -H "${API_HEADER}" --data-binary @- "${URI}/repos/${repo}/hooks" <<-EOF
	{
		"name": "web",
		"active": true,
		"events": ["push"],
		"config": {
		"url": "${JENKINS_HOOK_URL}",
		"jenkins_hook_url": "${JENKINS_HOOK_URL}",
		"content_type": "form"
	}
}
EOF
}

main(){
	repo=$1

	if [[ "$repo" == "" ]]; then
		echo "Pass a repo as the second argument: ex. jessfraz/jenkins-dsl" >&2
		exit 1
	fi

	create_hook "$repo"
}

main "$@"
