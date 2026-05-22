module "eks" {
  source  = "terraform-aws-modules/eks/aws"
  version = "~> 20.24"

  cluster_name    = "${local.name}-cluster"
  cluster_version = var.kubernetes_version

  cluster_endpoint_public_access       = true
  cluster_endpoint_public_access_cidrs = var.cluster_endpoint_public_access_cidrs

  enable_cluster_creator_admin_permissions = true

  vpc_id = module.vpc.vpc_id
  # Demo setup: nodes run in public subnets so we don't need a NAT Gateway.
  # The control plane ENIs stay in the private subnets (they don't need
  # internet egress, only VPC-internal connectivity to the kubelets).
  subnet_ids               = module.vpc.public_subnets
  control_plane_subnet_ids = module.vpc.private_subnets

  # Skip KMS envelope encryption of secrets for the demo (no KMS key, no
  # extra cost / IAM setup). Re-enable for production.
  create_kms_key            = false
  cluster_encryption_config = {}

  # Demo setup: keep CloudWatch control-plane logs for 1 day only.
  cloudwatch_log_group_retention_in_days = 1

  cluster_addons = {
    coredns = {
      most_recent = true
    }
    kube-proxy = {
      most_recent = true
    }
    vpc-cni = {
      most_recent = true
    }
    aws-ebs-csi-driver = {
      most_recent              = true
      service_account_role_arn = module.ebs_csi_irsa_role.iam_role_arn
    }
  }

  eks_managed_node_group_defaults = {
    ami_type       = "AL2023_x86_64_STANDARD"
    instance_types = var.node_instance_types
    capacity_type  = var.node_capacity_type
    disk_size      = var.node_disk_size
  }

  eks_managed_node_groups = {
    main = {
      name = "${local.name}-nodes"

      min_size     = var.node_min_size
      max_size     = var.node_max_size
      desired_size = var.node_desired_size

      labels = {
        role = "general"
      }
    }
  }
}

# IAM role for the AWS EBS CSI driver service account (kube-system/ebs-csi-controller-sa).
# Required so the EBS CSI driver can dynamically provision EBS volumes for the
# StatefulSets (RabbitMQ + 3 Postgres) defined in backend/infrastructure/k8s/.
module "ebs_csi_irsa_role" {
  source  = "terraform-aws-modules/iam/aws//modules/iam-role-for-service-accounts-eks"
  version = "~> 5.44"

  role_name             = "${local.name}-ebs-csi-irsa"
  attach_ebs_csi_policy = true

  oidc_providers = {
    main = {
      provider_arn               = module.eks.oidc_provider_arn
      namespace_service_accounts = ["kube-system:ebs-csi-controller-sa"]
    }
  }
}
