# SpringBootJspApp Jenkins Pipeline

This repository contains a standalone Jenkins pipeline for building, scanning, and deploying the Spring Boot JSP WAR app.

## What it does

- checks out source code
- builds the Maven WAR
- runs Trivy filesystem and image scans
- builds a Docker image
- optionally pushes the image to AWS ECR
- optionally deploys to AWS EKS using Kubernetes manifest `Deployment.yaml`

## Required environment variables

The pipeline is non-parameterized and uses Jenkins environment variables.

- `AWS_ACCOUNT_ID` — AWS account ID for ECR image tagging and ECR/EKS deployment (default: `962800862954`)
- `AWS_REGION` — AWS region for ECR and EKS (default: `us-east-1`)
- `DOCKER_REPOSITORY` — ECR repository and Docker image name (default: `myapp`)
- `EKS_CLUSTER_NAME` — EKS cluster name (default: `dev-eks`)
- `DEPLOYMENT_MANIFEST` — Kubernetes deployment manifest file (default: `Deployment.yaml`)
- `AWS_CREDENTIALS_ID` — Jenkins credential ID for AWS access keys (default: `AWS-cred`)

## Notes

- If `AWS_ACCOUNT_ID` is not set, the pipeline still builds the Docker image locally, but it skips ECR push and EKS deployment.
- The manifest `Deployment.yaml` uses `IMAGE_PLACEHOLDER`, which the pipeline replaces with the built image tag during deployment.
- Ensure the Jenkins credential referenced by `AWS_CREDENTIALS_ID` exists and has valid AWS access key permissions.

## Jenkins setup

1. Create a Jenkins pipeline job.
2. Point the job to this repository and use the `Jenkinsfile` at the repo root.
3. Configure the environment variables in the job or as global Jenkins environment variables.
4. Make sure the Jenkins agent has Docker, AWS CLI, Trivy, and kubectl installed.

## Deployment manifest

The Kubernetes manifest is stored in `Deployment.yaml` and defines:

- `Deployment` for `web-app`
- `Service` exposing port `80` mapped to `8080`

The image is injected into the manifest before `kubectl apply`.
