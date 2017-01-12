#!/bin/bash

# This script creates a deploy key (readonly) and saves it to your project.

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

create_deploy_key(){
	repo="$1"
	key="$2"

	echo "Creating jenkins deploy key for $repo"

	curl -i -sSL -XPOST -H "${AUTH_HEADER}" -H "${API_HEADER}" --data-binary @- "${URI}/repos/${repo}/keys" <<-EOF
	{
		"title": "jenkins@yoserver.cool",
		"read_only": true,
		"key": "${key}"
	}
	EOF
}

main(){
	repo=$1
	tmpd=$(mktemp -d --suffix=deploy_key)

	if [[ "$repo" == "" ]]; then
		echo "Pass a repo as the first argument: ex. jessfraz/jenkins-dsl" >&2
		exit 1
	fi

	# create the ssh key
	ssh-keygen -f "${tmpd}/id_ed25519" -t ed25519 -N ''

	create_deploy_key $repo "$(cat "${tmpd}/id_ed25519.pub")"

	echo "You can find your public and private key in $tmpd."
}

main $@
