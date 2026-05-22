variable "project_name" {
  description = "Project name used as a prefix for all created resources."
  type        = string
  default     = "sharks"
}

variable "environment" {
  description = "Deployment environment (e.g. dev, staging, prod)."
  type        = string
  default     = "dev"
}

variable "aws_region" {
  description = "AWS region to deploy to."
  type        = string
  default     = "us-east-1"
}

variable "vpc_cidr" {
  description = "CIDR block for the VPC."
  type        = string
  default     = "10.0.0.0/16"
}

variable "kubernetes_version" {
  description = "EKS control plane Kubernetes version."
  type        = string
  default     = "1.31"
}

variable "node_instance_types" {
  description = "EC2 instance types for the EKS managed node group."
  type        = list(string)
  default     = ["t3.medium"]
}

variable "node_capacity_type" {
  description = "Capacity type for nodes (ON_DEMAND or SPOT)."
  type        = string
  default     = "ON_DEMAND"
}

variable "node_min_size" {
  description = "Minimum size of the EKS managed node group."
  type        = number
  default     = 2
}

variable "node_max_size" {
  description = "Maximum size of the EKS managed node group."
  type        = number
  default     = 4
}

variable "node_desired_size" {
  description = "Desired size of the EKS managed node group."
  type        = number
  default     = 2
}

variable "node_disk_size" {
  description = "EBS root volume size (GiB) for each worker node."
  type        = number
  default     = 30
}

variable "service_names" {
  description = "List of service names to create ECR repositories for. One repo per Spring Boot service plus rabbitmq."
  type        = list(string)
  default = [
    "gateway",
    "auth",
    "user",
    "events",
    "ticketing",
    "admin",
    "rabbitmq",
  ]
}

variable "ecr_image_retention_count" {
  description = "Number of recent images to keep per ECR repository (older untagged/tagged images are expired)."
  type        = number
  default     = 10
}

variable "cluster_endpoint_public_access_cidrs" {
  description = "CIDR blocks allowed to access the EKS public API endpoint. Lock this down to your IP for production."
  type        = list(string)
  default     = ["0.0.0.0/0"]
}
