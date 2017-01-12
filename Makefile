all: projects/mirrors projects/repo-dockerfiles projects/forks

projects/mirrors: generate-mirrors.sh
	@echo "+ $@"
	./$< | column -t

projects/repo-dockerfiles: generate-repo-dockerfiles.sh
	@echo "+ $@"
	./$< | column -t

projects/forks: generate-update-forks.sh
	@echo "+ $@"
	./$< | column -t
