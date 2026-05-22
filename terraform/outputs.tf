output "aws_region" {
  description = "AWS region where the cluster is deployed."
  value       = var.aws_region
}

output "cluster_name" {
  description = "EKS cluster name."
  value       = module.eks.cluster_name
}

output "cluster_endpoint" {
  description = "Endpoint URL for the EKS Kubernetes API."
  value       = module.eks.cluster_endpoint
}

output "cluster_certificate_authority_data" {
  description = "Base64-encoded CA certificate for the EKS cluster."
  value       = module.eks.cluster_certificate_authority_data
  sensitive   = true
}

output "cluster_oidc_issuer_url" {
  description = "OIDC issuer URL for the EKS cluster (used to create additional IRSA roles)."
  value       = module.eks.cluster_oidc_issuer_url
}

output "vpc_id" {
  description = "ID of the VPC the cluster is deployed in."
  value       = module.vpc.vpc_id
}

output "private_subnet_ids" {
  description = "IDs of the private subnets used by EKS nodes."
  value       = module.vpc.private_subnets
}

output "public_subnet_ids" {
  description = "IDs of the public subnets used by load balancers."
  value       = module.vpc.public_subnets
}

output "ecr_repository_urls" {
  description = "Map of service name -> ECR repository URL. Tag images as <url>:<tag> and push."
  value       = { for k, repo in aws_ecr_repository.services : k => repo.repository_url }
}

output "configure_kubectl_command" {
  description = "Command to configure kubectl to talk to the new EKS cluster."
  value       = "aws eks update-kubeconfig --region ${var.aws_region} --name ${module.eks.cluster_name}"
}

output "ecr_login_command" {
  description = "Command to log Docker into ECR for image pushes."
  value       = "aws ecr get-login-password --region ${var.aws_region} | docker login --username AWS --password-stdin ${data.aws_caller_identity.current.account_id}.dkr.ecr.${var.aws_region}.amazonaws.com"
}

# Reminder for operators: the events service uploads cover images to
# Supabase Storage (S3-compatible API). Storage is hosted by Supabase, so no
# AWS S3 bucket / IAM is provisioned here. Before applying the K8s manifests,
# patch the `sharks-app-creds` secret in the `sharks` namespace with the
# Supabase Storage URL, bucket, access key and secret key.
output "events_supabase_storage_setup_hint" {
  description = "Post-apply: how to populate the Supabase Storage credentials the events service requires."
  value       = <<-EOT
    The events service requires Supabase Storage credentials to upload event
    cover images. After `terraform apply` and applying the K8s manifests, run:

      kubectl -n sharks patch secret sharks-app-creds --type merge -p '{
        "stringData": {
          "supabase-storage-url": "https://<project-ref>.storage.supabase.co/storage/v1/s3",
          "supabase-storage-bucket": "event-images",
          "supabase-storage-access-key": "<access-key>",
          "supabase-storage-secret-key": "<secret-key>"
        }
      }'

    Then restart the events deployment:

      kubectl -n sharks rollout restart deployment/events
  EOT
}

data "aws_caller_identity" "current" {}
