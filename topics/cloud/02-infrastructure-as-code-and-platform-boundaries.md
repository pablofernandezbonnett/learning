# Infrastructure as Code and Platform Boundaries

Infrastructure as code (IaC) records intended resources in versioned files
so changes can be reviewed and repeated. Terraform is one implementation.
It can create a network, database, container service, or access policy;
it does not decide whether those resources are the right architecture.

## Separate the Layers

- **Image build:** packages the application and runtime files.
- **Workload platform:** runs instances, routes traffic, and replaces failed
  instances. Kubernetes is one option; see
  [Kubernetes runtime basics](../devops/04-kubernetes-crash-course.md).
- **IaC:** creates or changes the infrastructure those workloads need.

Do not repeat Pod, Service, probe, and autoscaler definitions here. The IaC
decision is who owns the infrastructure change and how it is reviewed.

## Terraform's Core Loop

1. Declare resources and inputs in configuration.
2. Run `terraform plan` to inspect proposed changes.
3. Review effects on access, data, cost, and availability.
4. Run `terraform apply` through an authorized workflow.
5. Observe the result and handle drift or failed changes.

Terraform state maps configured resources to real objects. Treat it as
important operational data: store it securely, control concurrent writes,
back it up, and limit access. A plan can contain sensitive values, so do not
publish it casually.

A **resource** is managed by the configuration; a **data source** reads an
existing object. Variables provide inputs, outputs expose values, and modules
group reusable definitions. Keep modules small enough that a reviewer can
understand the changes they cause.

## Example: Adding a Database

The team proposes a managed database. The IaC change defines its network
placement, access, backup configuration, and required outputs for the
application. Review the plan for accidental public access or replacement
of an existing database. Then deploy the application configuration
separately and verify connectivity and recovery expectations.

IaC makes the change repeatable; it does not prove the backup can restore,
the schema is compatible, or the application has least privilege.

## When to Use Which Tool

A local Kubernetes lab may use manifests and `kubectl` to learn workload
objects. Add Terraform only when the exercise includes infrastructure
change review and state. In production, use the team's chosen source of
truth consistently so two tools do not silently fight over one resource.

## Review Questions

- Which resources does this configuration own?
- Where is state stored, and who can read or change it?
- Could the plan replace or expose a critical resource?
- How is drift detected and corrected?
- What application and data checks are needed after `apply`?

## Further Reading

- [Terraform state](https://developer.hashicorp.com/terraform/language/state)
- [Terraform plan](https://developer.hashicorp.com/terraform/cli/commands/plan)
