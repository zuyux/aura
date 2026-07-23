create table if not exists public.incident_evidence (
    id uuid primary key,
    report_id uuid not null references public.incident_reports(id) on delete cascade,
    type text not null,
    remote_url text,
    sha256_hash text not null,
    visibility text not null default 'PRIVATE',
    created_at_millis bigint not null,
    created_at timestamptz not null default now(),
    constraint incident_evidence_type_not_blank check (length(trim(type)) > 0),
    constraint incident_evidence_hash_not_blank check (length(trim(sha256_hash)) > 0),
    constraint incident_evidence_visibility_valid check (visibility in ('COMMUNITY', 'PRIVATE'))
);

create index if not exists incident_evidence_report_id_idx
on public.incident_evidence (report_id);

create table if not exists public.report_verifications (
    id uuid primary key,
    report_id uuid not null references public.incident_reports(id) on delete cascade,
    action text not null,
    device_id uuid,
    created_at_millis bigint not null,
    created_at timestamptz not null default now(),
    constraint report_verifications_action_valid
        check (action in ('ALSO_SEEN', 'SEEMS_FALSE', 'RESOLVED', 'HIDE_ALERT'))
);

create index if not exists report_verifications_report_id_idx
on public.report_verifications (report_id);

alter table public.incident_evidence enable row level security;
alter table public.report_verifications enable row level security;

grant select, insert on table public.incident_evidence to anon, authenticated;
grant select, insert on table public.report_verifications to anon, authenticated;

drop policy if exists "public can add incident evidence"
on public.incident_evidence;
create policy "public can add incident evidence"
on public.incident_evidence
for insert
to anon, authenticated
with check (
    exists (
        select 1
        from public.incident_reports report
        where report.id = report_id
          and report.visibility = 'COMMUNITY'
          and report.is_anonymous = true
    )
);

drop policy if exists "public can read community evidence"
on public.incident_evidence;
create policy "public can read community evidence"
on public.incident_evidence
for select
to anon, authenticated
using (
    visibility = 'COMMUNITY'
    and exists (
        select 1
        from public.incident_reports report
        where report.id = report_id
          and report.visibility = 'COMMUNITY'
    )
);

drop policy if exists "public can verify community reports"
on public.report_verifications;
create policy "public can verify community reports"
on public.report_verifications
for insert
to anon, authenticated
with check (
    action <> 'HIDE_ALERT'
    and exists (
        select 1
        from public.incident_reports report
        where report.id = report_id
          and report.visibility = 'COMMUNITY'
    )
);

drop policy if exists "public can read report verification counts"
on public.report_verifications;
create policy "public can read report verification counts"
on public.report_verifications
for select
to anon, authenticated
using (
    exists (
        select 1
        from public.incident_reports report
        where report.id = report_id
          and report.visibility = 'COMMUNITY'
    )
);
