# AWS Service Map for System Decisions

Use this note after the [general cloud method](./01-cloud-basics.md).
AWS product names are useful only when they answer a system question:
where code runs, how it is reached, where state lives, who can access it,
and how failures are observed.

## Map the Need to a Service Family

| Need | AWS examples | Decision to make |
| --- | --- | --- |
| Workload identity and access | IAM roles and policies | Which action may this workload perform on which resource? |
| Network placement and entry | VPC, subnets, security groups, load balancers | What is public, what stays private, and which traffic is allowed? |
| Virtual machines | EC2 | Does the workload need host-level control and can the team operate it? |
| Containers | ECS with Fargate, or EKS | Is managed container orchestration enough, or is Kubernetes a real requirement? |
| Triggered functions | Lambda | Do duration, startup, concurrency, and retry rules fit the work? |
| Relational data | RDS or Aurora | What schema, connection, backup, and recovery behavior is needed? |
| Objects and files | S3 | Which data is stored as objects, with what access and lifecycle rules? |
| Runtime signals and AWS activity | CloudWatch and CloudTrail | What shows application health, and what records changes to AWS resources? |
| Secrets and keys | Secrets Manager and KMS | How are secrets delivered and encryption keys controlled? |

This is a map, not a required stack. For example, a service may use a
managed database without Kubernetes or a function.

## Start With Identity and Data

Prefer workload identity and temporary credentials where the AWS service
supports them. Give each workload only the permissions it needs. Place
databases behind deliberate network and access boundaries; a private
subnet is useful but does not replace authentication or query safety.

Managed services reduce infrastructure operation, while the team still
owns application behavior, data access, cost, and recovery requirements.
Check the responsibility boundary for the exact service.

## Example: Order API

An order API could run as containers on ECS with Fargate, keep orders in
RDS, and put generated exports in S3. An IAM role grants the service
access to its bucket, while network rules restrict database traffic.
CloudWatch shows request and infrastructure signals; CloudTrail helps
audit AWS API actions. The team still designs idempotent order writes,
safe database changes, and restore tests.

If Kubernetes is already a justified platform choice, EKS may fit the
same application. If the work is a short event handler, Lambda may fit
instead. Choose from the workload, not from the service name.

## Review Questions

- Which part of this service does AWS operate, and which part does the team?
- What is exposed publicly, and who can reach the data store?
- Where do workload credentials come from?
- What happens when the compute service scales faster than the database?
- How will the team detect failure and test recovery?

## Further Reading

- [AWS compute decision guide](https://docs.aws.amazon.com/decision-guides/latest/decision-guides/choosing-aws-compute-service.html)
- [AWS shared responsibility](https://docs.aws.amazon.com/wellarchitected/latest/security-pillar/shared-responsibility.html)
