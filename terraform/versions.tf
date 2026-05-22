terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.70"
    }
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.30"
    }
    helm = {
      source  = "hashicorp/helm"
      version = "~> 2.15"
    }
  }

  # Recommended for production: configure an S3 backend for shared state.
  # backend "s3" {
  #   bucket         = "sharks-terraform-state"
  #   key            = "eks/terraform.tfstate"
  #   region         = "us-east-1"
  #   dynamodb_table = "sharks-terraform-locks"
  #   encrypt        = true
  # }
}
