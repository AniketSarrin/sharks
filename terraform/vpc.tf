module "vpc" {
  source  = "terraform-aws-modules/vpc/aws"
  version = "~> 5.13"

  name = "${local.name}-vpc"
  cidr = var.vpc_cidr

  azs             = local.azs
  public_subnets  = [for i, _ in local.azs : cidrsubnet(var.vpc_cidr, 4, i)]
  private_subnets = [for i, _ in local.azs : cidrsubnet(var.vpc_cidr, 4, i + 8)]

  # Demo setup: nodes run in public subnets, so a NAT Gateway is unnecessary.
  # Private subnets are still provisioned (the EKS control plane ENIs live
  # there), but they have no internet egress.
  enable_nat_gateway   = false
  single_nat_gateway   = false
  enable_dns_hostnames = true
  enable_dns_support   = true

  # Demo setup: auto-assign public IPs to instances (EKS nodes) launched in
  # the public subnets so they can reach the internet without a NAT Gateway
  # (pull container images, talk to the EKS API endpoint, etc.).
  map_public_ip_on_launch = true

  # EKS-required subnet tags so the AWS Load Balancer Controller / in-tree LB
  # provisioner can discover where to place public/internal load balancers.
  public_subnet_tags = {
    "kubernetes.io/role/elb" = 1
  }

  private_subnet_tags = {
    "kubernetes.io/role/internal-elb" = 1
  }
}
